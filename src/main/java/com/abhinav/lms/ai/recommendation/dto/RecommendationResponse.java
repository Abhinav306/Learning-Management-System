package com.abhinav.lms.ai.recommendation.dto;

import com.abhinav.lms.course.entity.DifficultyLevel;
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
public class RecommendationResponse {
    private UUID id;
    private String title;
    private String shortDescription;
    private String thumbnailUrl;
    private BigDecimal price;
    private DifficultyLevel difficulty;
    private String categoryName;
    private String reason;
}
