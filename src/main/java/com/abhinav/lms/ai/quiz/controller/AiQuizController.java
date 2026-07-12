package com.abhinav.lms.ai.quiz.controller;

import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationRequest;
import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationResponse;
import com.abhinav.lms.ai.quiz.service.AiQuizService;
import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "AI Quiz Generation", description = "Endpoints for auto-generating quizzes from lesson content using AI")
public class AiQuizController {

    private final AiQuizService aiQuizService;

    @PostMapping(AppConstants.API_V1 + "/courses/{courseId}/ai/quiz/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Auto-generate a quiz from lesson content (Instructor owner / Admin only)")
    public ApiResponse<AiQuizGenerationResponse> generateQuiz(
            @PathVariable UUID courseId,
            @Valid @RequestBody AiQuizGenerationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AiQuizGenerationResponse response = aiQuizService.generateQuiz(courseId, request, principal);
        return ApiResponse.created(response, "AI Quiz generated and saved successfully");
    }
}
