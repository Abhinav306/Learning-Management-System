package com.abhinav.lms.ai.quiz.service;

import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationRequest;
import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.UUID;

public interface AiQuizService {
    AiQuizGenerationResponse generateQuiz(UUID courseId, AiQuizGenerationRequest request, UserPrincipal currentUser);
}
