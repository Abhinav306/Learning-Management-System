package com.abhinav.lms.assignment.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRequest {

    @Size(max = 4000, message = "Submission content must not exceed 4000 characters")
    private String content;

    @Size(max = 500, message = "File URL must not exceed 500 characters")
    private String fileUrl;
}
