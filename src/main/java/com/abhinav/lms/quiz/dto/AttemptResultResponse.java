package com.abhinav.lms.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptResultResponse {

    private UUID attemptId;
    private UUID quizId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Double score;
    private Integer totalPoints;
    private boolean passed;
    private Integer attemptNumber;
    private Double scorePercentage;
}
