package com.abhinav.lms.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartAttemptResponse {

    private UUID attemptId;
    private UUID quizId;
    private Integer attemptNumber;
    private LocalDateTime startedAt;
    private List<QuestionResponse> questions;
}
