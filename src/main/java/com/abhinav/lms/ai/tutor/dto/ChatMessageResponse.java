package com.abhinav.lms.ai.tutor.dto;

import com.abhinav.lms.ai.tutor.entity.MessageRole;
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
public class ChatMessageResponse {
    private UUID id;
    private MessageRole role;
    private String content;
    private int tokenCount;
    private LocalDateTime createdAt;
}
