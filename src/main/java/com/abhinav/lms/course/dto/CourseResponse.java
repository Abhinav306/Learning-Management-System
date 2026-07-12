package com.abhinav.lms.course.dto;

import com.abhinav.lms.category.dto.CategoryResponse;
import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.entity.DifficultyLevel;
import com.abhinav.lms.user.dto.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private UUID id;
    private String title;
    private String shortDescription;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private DifficultyLevel difficulty;
    private CourseStatus status;
    private String language;
    private Double duration;
    private UserSummaryResponse instructor;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
