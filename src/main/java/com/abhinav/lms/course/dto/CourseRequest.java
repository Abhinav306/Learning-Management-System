package com.abhinav.lms.course.dto;

import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.entity.DifficultyLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    @NotBlank(message = "Course title is required")
    @Size(max = 255, message = "Course title must not exceed 255 characters")
    private String title;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    private String thumbnailUrl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be at least 0.00")
    private BigDecimal price;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty;

    @NotNull(message = "Course status is required")
    private CourseStatus status;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    private UUID categoryId;
}
