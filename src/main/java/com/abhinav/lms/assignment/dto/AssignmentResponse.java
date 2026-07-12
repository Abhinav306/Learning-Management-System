package com.abhinav.lms.assignment.dto;

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
public class AssignmentResponse {

    private UUID id;
    private String title;
    private String description;
    private String instructions;
    private Integer maxScore;
    private LocalDateTime dueDate;
    private UUID courseId;
    private UUID lessonId;
    private LocalDateTime createdAt;
}
