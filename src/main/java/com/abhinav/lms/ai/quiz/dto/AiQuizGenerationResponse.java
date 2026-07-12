package com.abhinav.lms.ai.quiz.dto;

import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiQuizGenerationResponse {

    private QuizResponse quiz;
    private List<QuestionResponse> questions;
    private AiMetadata metadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiMetadata {
        private String model;
        private Long promptTokens;
        private Long completionTokens;
        private Long totalTokens;
    }
}
