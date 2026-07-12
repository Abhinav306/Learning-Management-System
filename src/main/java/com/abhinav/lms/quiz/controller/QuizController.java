package com.abhinav.lms.quiz.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.quiz.dto.AttemptResultResponse;
import com.abhinav.lms.quiz.dto.QuestionRequest;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizRequest;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.quiz.dto.QuizSubmissionRequest;
import com.abhinav.lms.quiz.dto.StartAttemptResponse;
import com.abhinav.lms.quiz.service.QuizService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Quiz Management", description = "Endpoints for creating quizzes, templates, questions, and attempts auto-grading")
public class QuizController {

    private final QuizService quizService;

    @PostMapping(AppConstants.API_V1 + "/courses/{courseId}/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new quiz inside a course (Instructor owner / Admin only)")
    public ApiResponse<QuizResponse> createQuiz(
            @PathVariable UUID courseId,
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuizResponse response = quizService.createQuiz(courseId, request, principal);
        return ApiResponse.created(response, "Quiz created successfully");
    }

    @GetMapping(AppConstants.API_V1 + "/quizzes/{quizId}")
    @Operation(summary = "Get quiz details by ID (Public)")
    public ApiResponse<QuizResponse> getQuizById(@PathVariable UUID quizId) {
        QuizResponse response = quizService.getQuizById(quizId);
        return ApiResponse.success(response);
    }

    @GetMapping(AppConstants.API_V1 + "/courses/{courseId}/quizzes")
    @Operation(summary = "Get all quizzes in a course (Public)")
    public ApiResponse<List<QuizResponse>> getQuizzesByCourse(@PathVariable UUID courseId) {
        List<QuizResponse> response = quizService.getQuizzesByCourse(courseId);
        return ApiResponse.success(response);
    }

    @PutMapping(AppConstants.API_V1 + "/quizzes/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update quiz configuration settings (Instructor owner / Admin only)")
    public ApiResponse<QuizResponse> updateQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuizResponse response = quizService.updateQuiz(quizId, request, principal);
        return ApiResponse.success(response, "Quiz configuration updated successfully");
    }

    @DeleteMapping(AppConstants.API_V1 + "/quizzes/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete a quiz template (Instructor owner / Admin only)")
    public void deleteQuiz(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal UserPrincipal principal) {
        quizService.deleteQuiz(quizId, principal);
    }

    @PostMapping(AppConstants.API_V1 + "/quizzes/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Add a question to a quiz (Instructor owner / Admin only)")
    public ApiResponse<QuestionResponse> addQuestion(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuestionResponse response = quizService.addQuestion(quizId, request, principal);
        return ApiResponse.created(response, "Question added successfully");
    }

    @GetMapping(AppConstants.API_V1 + "/quizzes/{quizId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get all questions templates for editing (Instructor owner / Admin only)")
    public ApiResponse<List<QuestionResponse>> getQuestions(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<QuestionResponse> response = quizService.getQuestions(quizId, principal);
        return ApiResponse.success(response);
    }

    @PutMapping(AppConstants.API_V1 + "/quizzes/{quizId}/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update a quiz question template (Instructor owner / Admin only)")
    public ApiResponse<QuestionResponse> updateQuestion(
            @PathVariable UUID quizId,
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuestionResponse response = quizService.updateQuestion(quizId, questionId, request, principal);
        return ApiResponse.success(response, "Question updated successfully");
    }

    @DeleteMapping(AppConstants.API_V1 + "/quizzes/{quizId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete a quiz question (Instructor owner / Admin only)")
    public void deleteQuestion(
            @PathVariable UUID quizId,
            @PathVariable UUID questionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        quizService.deleteQuestion(quizId, questionId, principal);
    }

    @PostMapping(AppConstants.API_V1 + "/quizzes/{quizId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Start a new quiz attempt (Student owner / Admin only)")
    public ApiResponse<StartAttemptResponse> startAttempt(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal UserPrincipal principal) {
        StartAttemptResponse response = quizService.startAttempt(quizId, principal);
        return ApiResponse.created(response, "Quiz attempt started successfully");
    }

    @PostMapping(AppConstants.API_V1 + "/quizzes/{quizId}/attempts/{attemptId}/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Submit and auto-grade quiz attempt answers (Student owner / Admin only)")
    public ApiResponse<AttemptResultResponse> submitAttempt(
            @PathVariable UUID quizId,
            @PathVariable UUID attemptId,
            @Valid @RequestBody QuizSubmissionRequest submission,
            @AuthenticationPrincipal UserPrincipal principal) {
        AttemptResultResponse response = quizService.submitAttempt(quizId, attemptId, submission, principal);
        return ApiResponse.success(response, "Quiz attempt submitted and graded successfully");
    }

    @GetMapping(AppConstants.API_V1 + "/quizzes/{quizId}/attempts")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get all attempts for a quiz (Student owner / Admin only)")
    public ApiResponse<List<AttemptResultResponse>> getQuizAttempts(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<AttemptResultResponse> response = quizService.getQuizAttempts(quizId, principal);
        return ApiResponse.success(response);
    }
}
