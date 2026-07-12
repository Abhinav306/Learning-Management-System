package com.abhinav.lms.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class SectionRequest {

    @NotBlank(message = "Section title is required")
    @Size(max = 255, message = "Section title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be at least 0")
    private Integer sortOrder;
}
