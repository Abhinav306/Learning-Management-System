package com.abhinav.lms.ai.quiz.dto;

import com.abhinav.lms.quiz.entity.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiQuizGenerationRequest {

    @NotNull(message = "Lesson ID is required")
    private UUID lessonId;

    @Min(value = 1, message = "Number of questions must be at least 1")
    @Max(value = 20, message = "Number of questions cannot exceed 20")
    @Builder.Default
    private Integer numberOfQuestions = 5;

    private List<QuestionType> questionTypes;

    @Builder.Default
    private String difficulty = "MEDIUM";

    private String title;
}
