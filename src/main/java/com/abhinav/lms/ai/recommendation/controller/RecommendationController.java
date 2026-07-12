package com.abhinav.lms.ai.recommendation.controller;

import com.abhinav.lms.ai.recommendation.dto.RecommendationResponse;
import com.abhinav.lms.ai.recommendation.service.RecommendationService;
import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_V1 + "/ai/recommendations")
@RequiredArgsConstructor
@Tag(name = "AI Recommendations", description = "Endpoints for course recommendation engines")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get personalized AI recommendations for the student")
    public ApiResponse<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendationResponse> recommendations = recommendationService.getPersonalizedRecommendations(principal, limit);
        return ApiResponse.success(recommendations);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular courses (Fallback / Public Browse)")
    public ApiResponse<List<RecommendationResponse>> getPopularCourses(
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendationResponse> popular = recommendationService.getPopularCourses(limit);
        return ApiResponse.success(popular);
    }
}
