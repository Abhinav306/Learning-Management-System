package com.abhinav.lms.notification.dto;

import com.abhinav.lms.notification.entity.NotificationType;
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
public class NotificationResponse {

    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean read;
    private LocalDateTime readAt;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
