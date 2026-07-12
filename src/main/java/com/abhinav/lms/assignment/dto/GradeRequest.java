package com.abhinav.lms.assignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequest {

    @NotNull(message = "Grade is required")
    @Min(value = 0, message = "Grade must be at least 0")
    private Integer grade;

    @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
    private String feedback;
}
