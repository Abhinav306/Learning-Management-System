package com.abhinav.lms.review.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.review.dto.CourseReviewsSummaryResponse;
import com.abhinav.lms.review.dto.ReviewRequest;
import com.abhinav.lms.review.dto.ReviewResponse;
import com.abhinav.lms.review.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Course Review Management", description = "Endpoints for student ratings and course reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(AppConstants.API_V1 + "/courses/{courseId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Submit a rating and comment review for a course (Enrolled Student / Admin only)")
    public ApiResponse<ReviewResponse> createReview(
            @PathVariable UUID courseId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ReviewResponse response = reviewService.createReview(courseId, request, principal);
        return ApiResponse.created(response, "Review submitted successfully");
    }

    @PutMapping(AppConstants.API_V1 + "/courses/{courseId}/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Update an existing course review rating/comment (Student owner / Admin only)")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable UUID courseId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ReviewResponse response = reviewService.updateReview(courseId, reviewId, request, principal);
        return ApiResponse.success(response, "Review updated successfully");
    }

    @DeleteMapping(AppConstants.API_V1 + "/courses/{courseId}/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Delete a course review (Student owner / Admin only)")
    public void deleteReview(
            @PathVariable UUID courseId,
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.deleteReview(courseId, reviewId, principal);
    }

    @GetMapping(AppConstants.API_V1 + "/courses/{courseId}/reviews")
    @Operation(summary = "Get all reviews and rating aggregates for a course (Public)")
    public ApiResponse<CourseReviewsSummaryResponse> getCourseReviews(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        CourseReviewsSummaryResponse response = reviewService.getCourseReviews(courseId, page, size);
        return ApiResponse.success(response);
    }
}
