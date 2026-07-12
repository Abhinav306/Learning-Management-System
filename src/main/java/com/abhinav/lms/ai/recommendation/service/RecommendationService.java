package com.abhinav.lms.ai.recommendation.service;

import com.abhinav.lms.ai.recommendation.dto.RecommendationResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.List;

public interface RecommendationService {
    List<RecommendationResponse> getPersonalizedRecommendations(UserPrincipal currentUser, int limit);
    List<RecommendationResponse> getPopularCourses(int limit);
}
