package com.abhinav.lms.enrollment.dto;

import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
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
public class EnrollmentResponse {

    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID courseId;
    private String courseTitle;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private EnrollmentStatus status;
    private Double progress;
}
