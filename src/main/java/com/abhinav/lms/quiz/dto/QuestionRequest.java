package com.abhinav.lms.quiz.dto;

import com.abhinav.lms.quiz.entity.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(max = 4000, message = "Question text must not exceed 4000 characters")
    private String questionText;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    private List<String> options;

    @NotBlank(message = "Correct answer is required")
    @Size(max = 1000, message = "Correct answer must not exceed 1000 characters")
    private String correctAnswer;

    @Size(max = 1000, message = "Explanation must not exceed 1000 characters")
    private String explanation;

    @Min(value = 1, message = "Points must be at least 1")
    @Builder.Default
    private Integer points = 1;

    @NotNull(message = "Sort order is required")
    private Integer sortOrder;
}
