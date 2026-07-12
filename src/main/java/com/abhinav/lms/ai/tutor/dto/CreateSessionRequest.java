package com.abhinav.lms.ai.tutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateSessionRequest {

    @NotBlank(message = "Session title is required")
    @Size(max = 255, message = "Session title cannot exceed 255 characters")
    private String title;

    private UUID courseId;
}
