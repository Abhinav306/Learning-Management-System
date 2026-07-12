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
public class QuizResponse {

    private UUID id;
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer passingScore;
    private Integer maxAttempts;
    private boolean shuffleQuestions;
    private UUID courseId;
    private UUID lessonId;
    private LocalDateTime createdAt;
}
