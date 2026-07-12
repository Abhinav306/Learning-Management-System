package com.abhinav.lms.quiz.service;

import com.abhinav.lms.quiz.dto.AttemptResultResponse;
import com.abhinav.lms.quiz.dto.QuestionRequest;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizRequest;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.quiz.dto.QuizSubmissionRequest;
import com.abhinav.lms.quiz.dto.StartAttemptResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    QuizResponse createQuiz(UUID courseId, QuizRequest request, UserPrincipal currentUser);

    QuizResponse getQuizById(UUID quizId);

    List<QuizResponse> getQuizzesByCourse(UUID courseId);

    QuizResponse updateQuiz(UUID quizId, QuizRequest request, UserPrincipal currentUser);

    void deleteQuiz(UUID quizId, UserPrincipal currentUser);

    QuestionResponse addQuestion(UUID quizId, QuestionRequest request, UserPrincipal currentUser);

    List<QuestionResponse> getQuestions(UUID quizId, UserPrincipal currentUser);

    QuestionResponse updateQuestion(UUID quizId, UUID questionId, QuestionRequest request, UserPrincipal currentUser);

    void deleteQuestion(UUID quizId, UUID questionId, UserPrincipal currentUser);

    StartAttemptResponse startAttempt(UUID quizId, UserPrincipal currentUser);

    AttemptResultResponse submitAttempt(UUID quizId, UUID attemptId, QuizSubmissionRequest submission, UserPrincipal currentUser);

    List<AttemptResultResponse> getQuizAttempts(UUID quizId, UserPrincipal currentUser);
}
