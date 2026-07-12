package com.abhinav.lms.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerRequest {

    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotBlank(message = "Selected answer option is required")
    private String selectedAnswer;
}
