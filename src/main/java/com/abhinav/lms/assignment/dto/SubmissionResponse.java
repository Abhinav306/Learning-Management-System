package com.abhinav.lms.assignment.dto;

import com.abhinav.lms.assignment.entity.SubmissionStatus;
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
public class SubmissionResponse {

    private UUID id;
    private UUID assignmentId;
    private UUID studentId;
    private String studentName;
    private String content;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Integer grade;
    private String feedback;
    private SubmissionStatus status;
    private LocalDateTime gradedAt;
    private String gradedByName;
}
