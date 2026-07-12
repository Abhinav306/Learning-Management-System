package com.abhinav.lms.ai.tutor.dto;

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
public class ChatSessionResponse {
    private UUID id;
    private String title;
    private UUID courseId;
    private String courseTitle;
    private int totalTokens;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
