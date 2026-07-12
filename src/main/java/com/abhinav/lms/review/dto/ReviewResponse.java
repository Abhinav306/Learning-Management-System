package com.abhinav.lms.review.dto;

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
public class ReviewResponse {

    private UUID id;
    private UUID courseId;
    private UUID studentId;
    private String studentName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
