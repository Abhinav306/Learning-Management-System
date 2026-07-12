package com.abhinav.lms.quiz.service;

import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.course.repository.LessonRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.quiz.dto.AttemptResultResponse;
import com.abhinav.lms.quiz.dto.QuestionRequest;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizAnswerRequest;
import com.abhinav.lms.quiz.dto.QuizRequest;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.quiz.dto.QuizSubmissionRequest;
import com.abhinav.lms.quiz.dto.StartAttemptResponse;
import com.abhinav.lms.quiz.entity.Quiz;
import com.abhinav.lms.quiz.entity.QuizAnswer;
import com.abhinav.lms.quiz.entity.QuizAttempt;
import com.abhinav.lms.quiz.entity.QuizQuestion;
import com.abhinav.lms.quiz.mapper.QuizMapper;
import com.abhinav.lms.quiz.repository.QuizAnswerRepository;
import com.abhinav.lms.quiz.repository.QuizAttemptRepository;
import com.abhinav.lms.quiz.repository.QuizQuestionRepository;
import com.abhinav.lms.quiz.repository.QuizRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.abhinav.lms.notification.entity.NotificationType;
import com.abhinav.lms.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAnswerRepository answerRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final QuizMapper quizMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public QuizResponse createQuiz(UUID courseId, QuizRequest request, UserPrincipal currentUser) {
        log.info("Creating quiz in course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        checkCourseOwnershipOrAdmin(course, currentUser);

        Lesson lesson = null;
        if (request.getLessonId() != null) {
            lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", request.getLessonId()));
            if (!lesson.getSection().getCourse().getId().equals(courseId)) {
                throw new BusinessException("Lesson does not belong to the specified course");
            }
        }

        Quiz quiz = quizMapper.toEntity(request);
        quiz.setCourse(course);
        quiz.setLesson(lesson);

        Quiz saved = quizRepository.save(quiz);
        log.info("Quiz created successfully with ID: {}", saved.getId());
        return quizMapper.toResponse(saved);
    }

    @Override
    public QuizResponse getQuizById(UUID quizId) {
        log.debug("Fetching quiz ID: {}", quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        return quizMapper.toResponse(quiz);
    }

    @Override
    public List<QuizResponse> getQuizzesByCourse(UUID courseId) {
        log.debug("Fetching all quizzes in course ID: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizMapper.toResponseList(quizzes);
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(UUID quizId, QuizRequest request, UserPrincipal currentUser) {
        log.info("Updating quiz ID: {}", quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        checkCourseOwnershipOrAdmin(quiz.getCourse(), currentUser);

        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", request.getLessonId()));
            if (!lesson.getSection().getCourse().getId().equals(quiz.getCourse().getId())) {
                throw new BusinessException("Lesson does not belong to the specified course");
            }
            quiz.setLesson(lesson);
        } else {
            quiz.setLesson(null);
        }

        quizMapper.updateEntityFromRequest(request, quiz);
        Quiz updated = quizRepository.save(quiz);
        log.info("Quiz updated successfully with ID: {}", quizId);
        return quizMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID quizId, UserPrincipal currentUser) {
        log.info("Deleting quiz ID: {}", quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        checkCourseOwnershipOrAdmin(quiz.getCourse(), currentUser);

        quizRepository.delete(quiz);
        log.info("Quiz deleted successfully with ID: {}", quizId);
    }

    @Override
    @Transactional
    public QuestionResponse addQuestion(UUID quizId, QuestionRequest request, UserPrincipal currentUser) {
        log.info("Adding question to quiz ID: {}", quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        checkCourseOwnershipOrAdmin(quiz.getCourse(), currentUser);

        QuizQuestion question = quizMapper.toEntity(request);
        question.setQuiz(quiz);

        QuizQuestion saved = questionRepository.save(question);
        log.info("Question added successfully with ID: {}", saved.getId());
        return quizMapper.toResponse(saved);
    }

    @Override
    public List<QuestionResponse> getQuestions(UUID quizId, UserPrincipal currentUser) {
        log.debug("Fetching questions list for quiz ID: {}", quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        checkCourseOwnershipOrAdmin(quiz.getCourse(), currentUser);

        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        return quizMapper.toQuestionResponseList(questions);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(UUID quizId, UUID questionId, QuestionRequest request, UserPrincipal currentUser) {
        log.info("Updating question ID: {} in quiz ID: {}", questionId, quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        checkCourseOwnershipOrAdmin(quiz.getCourse(), currentUser);

        QuizQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        if (!question.getQuiz().getId().equals(quizId)) {
            throw new BusinessException("Question does not belong to the specified quiz");
        }

        quizMapper.updateEntityFromRequest(request, question);
        QuizQuestion updated = questionRepository.save(question);
        log.info("Question updated successfully with ID: {}", questionId);
        return quizMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID quizId, UUID questionId, UserPrincipal currentUser) {
        log.info("Deleting question ID: {} in quiz ID: {}", questionId, quizId);
        Quiz quiz = findQuizOrThrow(quizId);
        checkCourseOwnershipOrAdmin(quiz.getCourse(), currentUser);

        QuizQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        if (!question.getQuiz().getId().equals(quizId)) {
            throw new BusinessException("Question does not belong to the specified quiz");
        }

        questionRepository.delete(question);
        log.info("Question deleted successfully with ID: {}", questionId);
    }

    @Override
    @Transactional
    public StartAttemptResponse startAttempt(UUID quizId, UserPrincipal currentUser) {
        log.info("Student ID: {} starting quiz attempt for quiz ID: {}", currentUser.getId(), quizId);
        Quiz quiz = findQuizOrThrow(quizId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), quiz.getCourse().getId())
                .orElseThrow(() -> new BusinessException("Only enrolled students can attempt quizzes"));

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED || enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessException("Cannot start quizzes on an inactive course enrollment");
        }

        long attemptsCount = attemptRepository.countByQuizIdAndStudentId(quizId, currentUser.getId());
        if (attemptsCount >= quiz.getMaxAttempts()) {
            throw new BusinessException("You have reached the maximum number of attempts for this quiz");
        }

        User student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startedAt(LocalDateTime.now())
                .attemptNumber((int) (attemptsCount + 1))
                .passed(false)
                .build();

        QuizAttempt savedAttempt = attemptRepository.save(attempt);

        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        List<QuestionResponse> questionsDto = quizMapper.toQuestionResponseList(questions);

        // Security: clear correct answers and explanation details from Student payloads
        questionsDto.forEach(q -> {
            q.setCorrectAnswer(null);
            q.setExplanation(null);
        });

        if (quiz.isShuffleQuestions()) {
            Collections.shuffle(questionsDto);
        }

        return StartAttemptResponse.builder()
                .attemptId(savedAttempt.getId())
                .quizId(quizId)
                .attemptNumber(savedAttempt.getAttemptNumber())
                .startedAt(savedAttempt.getStartedAt())
                .questions(questionsDto)
                .build();
    }

    @Override
    @Transactional
    public AttemptResultResponse submitAttempt(UUID quizId, UUID attemptId, QuizSubmissionRequest submission, UserPrincipal currentUser) {
        log.info("Student ID: {} submitting quiz attempt ID: {}", currentUser.getId(), attemptId);
        Quiz quiz = findQuizOrThrow(quizId);

        QuizAttempt attempt = attemptRepository.findByIdAndStudentId(attemptId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        if (!attempt.getQuiz().getId().equals(quizId)) {
            throw new BusinessException("Quiz attempt does not belong to the specified quiz");
        }

        if (attempt.getCompletedAt() != null) {
            throw new BusinessException("This quiz attempt has already been submitted");
        }

        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        Map<UUID, QuizQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));

        double totalScore = 0.0;
        int maxPoints = 0;
        List<QuizAnswer> answers = new ArrayList<>();

        for (QuizAnswerRequest ansReq : submission.getAnswers()) {
            QuizQuestion question = questionMap.get(ansReq.getQuestionId());
            if (question == null) {
                throw new BusinessException("Question ID: " + ansReq.getQuestionId() + " does not belong to this quiz");
            }

            boolean isCorrect = question.getCorrectAnswer().trim().equalsIgnoreCase(ansReq.getSelectedAnswer().trim());
            int pointsScored = isCorrect ? question.getPoints() : 0;

            totalScore += pointsScored;
            maxPoints += question.getPoints();

            QuizAnswer answer = QuizAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedAnswer(ansReq.getSelectedAnswer())
                    .correct(isCorrect)
                    .pointsEarned(pointsScored)
                    .build();

            answers.add(answer);
        }

        // Tally missing questions as zero points
        for (QuizQuestion q : questions) {
            boolean answered = submission.getAnswers().stream()
                    .anyMatch(a -> a.getQuestionId().equals(q.getId()));
            if (!answered) {
                maxPoints += q.getPoints();
            }
        }

        answerRepository.saveAll(answers);

        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setScore(totalScore);
        attempt.setTotalPoints(maxPoints);

        double pct = maxPoints == 0 ? 0.0 : (totalScore / maxPoints) * 100.0;
        attempt.setPassed(pct >= quiz.getPassingScore());

        QuizAttempt graded = attemptRepository.save(attempt);
        log.info("Quiz attempt ID: {} graded successfully. Passed: {}, Score: {}%", attemptId, graded.isPassed(), pct);

        // Notify student
        notificationService.sendNotification(
                graded.getStudent(),
                "Quiz Attempt Completed",
                "You scored " + String.format("%.1f", pct) + "% on " + quiz.getTitle() + " attempt #" + graded.getAttemptNumber(),
                NotificationType.QUIZ,
                quiz.getId().toString(),
                "QUIZ"
        );

        return quizMapper.toResponse(graded);
    }

    @Override
    public List<AttemptResultResponse> getQuizAttempts(UUID quizId, UserPrincipal currentUser) {
        log.debug("Fetching attempts list for quiz ID: {}", quizId);
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz", "id", quizId);
        }
        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStudentIdOrderByStartedAtDesc(quizId, currentUser.getId());
        return quizMapper.toAttemptResponseList(attempts);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private Quiz findQuizOrThrow(UUID quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
    }

    private void checkCourseOwnershipOrAdmin(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this course quiz configuration");
        }
    }
}
