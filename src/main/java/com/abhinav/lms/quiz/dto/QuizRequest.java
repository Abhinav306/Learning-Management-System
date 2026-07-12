package com.abhinav.lms.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class QuizRequest {

    @NotBlank(message = "Quiz title is required")
    @Size(max = 255, message = "Quiz title must not exceed 255 characters")
    private String title;

    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimit;

    @Min(value = 0, message = "Passing score must be at least 0%")
    @Max(value = 100, message = "Passing score must not exceed 100%")
    private Integer passingScore;

    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts;

    private boolean shuffleQuestions;

    private UUID lessonId;
}
