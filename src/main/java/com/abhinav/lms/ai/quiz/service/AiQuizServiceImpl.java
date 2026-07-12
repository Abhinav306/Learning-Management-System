package com.abhinav.lms.ai.quiz.service;

import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationRequest;
import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.course.repository.LessonRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.quiz.entity.QuestionType;
import com.abhinav.lms.quiz.entity.Quiz;
import com.abhinav.lms.quiz.entity.QuizQuestion;
import com.abhinav.lms.quiz.mapper.QuizMapper;
import com.abhinav.lms.quiz.repository.QuizQuestionRepository;
import com.abhinav.lms.quiz.repository.QuizRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiQuizServiceImpl implements AiQuizService {

    private final ChatModel chatModel;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizMapper quizMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AiQuizGenerationResponse generateQuiz(UUID courseId, AiQuizGenerationRequest request, UserPrincipal currentUser) {
        log.info("Generating AI quiz for course ID: {}, lesson ID: {}", courseId, request.getLessonId());

        // 1. Validations
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        checkCourseOwnershipOrAdmin(course, currentUser);

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", request.getLessonId()));

        if (!lesson.getSection().getCourse().getId().equals(courseId)) {
            throw new BusinessException("Lesson does not belong to the specified course");
        }

        if (lesson.getContent() == null || lesson.getContent().trim().length() < 50) {
            throw new BusinessException("Lesson content is too short to generate a quiz. Please ensure it has substantial text.");
        }

        // 2. Build AI Prompt
        String allowedTypes = request.getQuestionTypes() == null || request.getQuestionTypes().isEmpty()
                ? "SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER"
                : request.getQuestionTypes().stream().map(Enum::name).collect(Collectors.joining(", "));

        String systemInstruction = String.format(
                "You are an expert educator. Your task is to generate exactly %d quiz questions based on the lesson content provided by the user.\n" +
                "Follow these strict rules:\n" +
                "1. Question types allowed: [%s].\n" +
                "2. Difficulty level: %s.\n" +
                "3. Formatting:\n" +
                "   - For SINGLE_CHOICE: provide exactly 4 options. The correctAnswer must match one of these options exactly.\n" +
                "   - For MULTIPLE_CHOICE: provide exactly 4 options. The correctAnswer must be a comma-separated list matching the correct options exactly.\n" +
                "   - For TRUE_FALSE: provide exactly 2 options: [\"True\", \"False\"]. The correctAnswer must be exactly \"True\" or \"False\".\n" +
                "   - For SHORT_ANSWER: options list must be empty. The correctAnswer is the text answer.\n" +
                "4. Points: assign realistic points (e.g. 1 to 5 points per question).\n" +
                "5. Output: You MUST respond ONLY with a raw JSON array matching this format (do not wrap it in markdown code block, no backticks, no comments, just raw JSON):\n" +
                "[\n" +
                "  {\n" +
                "    \"questionText\": \"string\",\n" +
                "    \"type\": \"SINGLE_CHOICE|MULTIPLE_CHOICE|TRUE_FALSE|SHORT_ANSWER\",\n" +
                "    \"options\": [\"string\", \"string\", ...],\n" +
                "    \"correctAnswer\": \"string\",\n" +
                "    \"explanation\": \"string\",\n" +
                "    \"points\": 1\n" +
                "  }\n" +
                "]",
                request.getNumberOfQuestions(),
                allowedTypes,
                request.getDifficulty()
        );

        String userPrompt = String.format(
                "LESSON TITLE: %s\nLESSON CONTENT:\n%s",
                lesson.getTitle(),
                lesson.getContent()
        );

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemInstruction),
                new UserMessage(userPrompt)
        ));

        // 3. Call AI
        log.info("Sending request to ChatModel...");
        ChatResponse response = chatModel.call(prompt);

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            throw new BusinessException("Failed to receive a valid response from the AI model.");
        }

        String rawJson = response.getResult().getOutput().getText();
        String cleanedJson = cleanJsonString(rawJson);
        log.debug("Cleaned JSON response: {}", cleanedJson);

        // 4. Parse JSON Response
        List<GeneratedQuestion> generatedQuestions;
        try {
            generatedQuestions = objectMapper.readValue(cleanedJson, new TypeReference<List<GeneratedQuestion>>() {});
        } catch (Exception e) {
            log.error("Failed to parse AI response as JSON array. Raw content: {}", rawJson, e);
            throw new BusinessException("AI model failed to return structured question data. Try again or modify the request.");
        }

        if (generatedQuestions == null || generatedQuestions.isEmpty()) {
            throw new BusinessException("No quiz questions were generated by the AI model.");
        }

        // 5. Persist Quiz and Questions
        String quizTitle = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle().trim()
                : "AI Generated Quiz: " + lesson.getTitle();

        Quiz quiz = Quiz.builder()
                .title(quizTitle)
                .description("Automatically generated by AI from the lesson: " + lesson.getTitle())
                .timeLimit(request.getNumberOfQuestions() * 2) // default 2 minutes per question
                .passingScore(60)
                .maxAttempts(3)
                .shuffleQuestions(false)
                .course(course)
                .lesson(lesson)
                .aiGenerated(true)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);

        List<QuizQuestion> quizQuestions = new ArrayList<>();
        int sortOrder = 1;
        for (GeneratedQuestion gq : generatedQuestions) {
            // Options sanitization/fallback
            List<String> options = gq.getOptions();
            if (options == null) {
                options = new ArrayList<>();
            }
            if (gq.getType() == QuestionType.TRUE_FALSE && options.isEmpty()) {
                options = List.of("True", "False");
            }

            QuizQuestion question = QuizQuestion.builder()
                    .quiz(savedQuiz)
                    .questionText(gq.getQuestionText() != null ? gq.getQuestionText() : "Untitled Question")
                    .type(gq.getType() != null ? gq.getType() : QuestionType.SINGLE_CHOICE)
                    .options(options)
                    .correctAnswer(gq.getCorrectAnswer() != null ? gq.getCorrectAnswer() : "")
                    .explanation(gq.getExplanation())
                    .points(gq.getPoints() != null ? gq.getPoints() : 1)
                    .sortOrder(sortOrder++)
                    .build();

            quizQuestions.add(questionRepository.save(question));
        }

        // 6. Map to DTO Response
        QuizResponse quizResponse = quizMapper.toResponse(savedQuiz);
        List<QuestionResponse> questionResponses = quizMapper.toQuestionResponseList(quizQuestions);

        // Usage details
        Usage usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        String modelName = response.getResult().getOutput().getMetadata().get("modelName") != null
                ? response.getResult().getOutput().getMetadata().get("modelName").toString()
                : "gpt-4o-mini";

        AiQuizGenerationResponse.AiMetadata metadata = AiQuizGenerationResponse.AiMetadata.builder()
                .model(modelName)
                .promptTokens(usage != null && usage.getPromptTokens() != null ? usage.getPromptTokens() : 0L)
                .completionTokens(usage != null && usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0L)
                .totalTokens(usage != null && usage.getTotalTokens() != null ? usage.getTotalTokens() : 0L)
                .build();

        return AiQuizGenerationResponse.builder()
                .quiz(quizResponse)
                .questions(questionResponses)
                .metadata(metadata)
                .build();
    }

    private void checkCourseOwnershipOrAdmin(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this course quiz configuration");
        }
    }

    private String cleanJsonString(String rawJson) {
        if (rawJson == null) {
            return "";
        }
        String cleaned = rawJson.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratedQuestion {
        private String questionText;
        private QuestionType type;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
        private Integer points;
    }
}
