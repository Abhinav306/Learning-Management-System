package com.abhinav.lms.ai.quiz.service;

import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationRequest;
import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.entity.Section;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.course.repository.LessonRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.quiz.entity.QuestionType;
import com.abhinav.lms.quiz.entity.Quiz;
import com.abhinav.lms.quiz.entity.QuizQuestion;
import com.abhinav.lms.quiz.mapper.QuizMapper;
import com.abhinav.lms.quiz.repository.QuizQuestionRepository;
import com.abhinav.lms.quiz.repository.QuizRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiQuizServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository questionRepository;

    @Mock
    private QuizMapper quizMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiQuizServiceImpl aiQuizService;

    private UserPrincipal instructorPrincipal;
    private User instructor;
    private Course course;
    private Section section;
    private Lesson lesson;
    private UUID courseId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();

        instructor = User.builder()
                .id(UUID.randomUUID())
                .email("instructor@test.com")
                .role(UserRole.INSTRUCTOR)
                .build();

        instructorPrincipal = UserPrincipal.create(instructor);

        course = Course.builder()
                .id(courseId)
                .title("Spring Boot Masterclass")
                .instructor(instructor)
                .build();

        section = Section.builder()
                .id(UUID.randomUUID())
                .course(course)
                .build();

        lesson = Lesson.builder()
                .id(lessonId)
                .title("Dependency Injection")
                .content("Dependency Injection is a technique in which an object receives other objects that it depends on. This helps achieve loose coupling.")
                .section(section)
                .build();
    }

    @Test
    void generateQuiz_Success() {
        AiQuizGenerationRequest request = AiQuizGenerationRequest.builder()
                .lessonId(lessonId)
                .numberOfQuestions(1)
                .difficulty("MEDIUM")
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);

        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockMessage);

        String jsonQuestions = "[" +
                "  {" +
                "    \"questionText\": \"What is DI?\"," +
                "    \"type\": \"SINGLE_CHOICE\"," +
                "    \"options\": [\"Loose Coupling\", \"Tight Coupling\", \"No Coupling\", \"None\"]," +
                "    \"correctAnswer\": \"Loose Coupling\"," +
                "    \"explanation\": \"DI helps achieve loose coupling.\"," +
                "    \"points\": 2" +
                "  }" +
                "]";
        when(mockMessage.getText()).thenReturn(jsonQuestions);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        Quiz savedQuiz = Quiz.builder()
                .id(UUID.randomUUID())
                .title("AI Generated Quiz: Dependency Injection")
                .course(course)
                .lesson(lesson)
                .aiGenerated(true)
                .build();

        QuizQuestion savedQuestion = QuizQuestion.builder()
                .id(UUID.randomUUID())
                .quiz(savedQuiz)
                .questionText("What is DI?")
                .type(QuestionType.SINGLE_CHOICE)
                .options(List.of("Loose Coupling", "Tight Coupling", "No Coupling", "None"))
                .correctAnswer("Loose Coupling")
                .explanation("DI helps achieve loose coupling.")
                .points(2)
                .sortOrder(1)
                .build();

        when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);
        when(questionRepository.save(any(QuizQuestion.class))).thenReturn(savedQuestion);

        QuizResponse quizResponse = QuizResponse.builder()
                .id(savedQuiz.getId())
                .title(savedQuiz.getTitle())
                .build();

        QuestionResponse questionResponse = QuestionResponse.builder()
                .id(savedQuestion.getId())
                .questionText(savedQuestion.getQuestionText())
                .build();

        when(quizMapper.toResponse(savedQuiz)).thenReturn(quizResponse);
        when(quizMapper.toQuestionResponseList(any())).thenReturn(List.of(questionResponse));

        AiQuizGenerationResponse result = aiQuizService.generateQuiz(courseId, request, instructorPrincipal);

        assertNotNull(result);
        assertEquals(quizResponse.getTitle(), result.getQuiz().getTitle());
        assertEquals(1, result.getQuestions().size());
        assertEquals(questionResponse.getQuestionText(), result.getQuestions().get(0).getQuestionText());
    }

    @Test
    void generateQuiz_LessonNotBelongToCourse_ThrowsException() {
        Course otherCourse = Course.builder()
                .id(UUID.randomUUID())
                .instructor(instructor)
                .build();
        Section otherSection = Section.builder().course(otherCourse).build();
        Lesson otherLesson = Lesson.builder()
                .id(lessonId)
                .content("Valid content long enough to pass validation...")
                .section(otherSection)
                .build();

        AiQuizGenerationRequest request = AiQuizGenerationRequest.builder()
                .lessonId(lessonId)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(otherLesson));

        assertThrows(BusinessException.class, () ->
                aiQuizService.generateQuiz(courseId, request, instructorPrincipal)
        );
    }

    @Test
    void generateQuiz_LessonContentTooShort_ThrowsException() {
        lesson.setContent("Short"); // Too short!

        AiQuizGenerationRequest request = AiQuizGenerationRequest.builder()
                .lessonId(lessonId)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        assertThrows(BusinessException.class, () ->
                aiQuizService.generateQuiz(courseId, request, instructorPrincipal)
        );
    }
}
