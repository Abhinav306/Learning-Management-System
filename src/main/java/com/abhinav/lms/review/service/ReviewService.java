package com.abhinav.lms.review.service;

import com.abhinav.lms.review.dto.CourseReviewsSummaryResponse;
import com.abhinav.lms.review.dto.ReviewRequest;
import com.abhinav.lms.review.dto.ReviewResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse createReview(UUID courseId, ReviewRequest request, UserPrincipal currentUser);

    ReviewResponse updateReview(UUID courseId, UUID reviewId, ReviewRequest request, UserPrincipal currentUser);

    void deleteReview(UUID courseId, UUID reviewId, UserPrincipal currentUser);

    CourseReviewsSummaryResponse getCourseReviews(UUID courseId, int page, int size);
}
