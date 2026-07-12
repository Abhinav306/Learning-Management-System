package com.abhinav.lms.notification.service;

import com.abhinav.lms.notification.dto.NotificationResponse;
import com.abhinav.lms.notification.entity.NotificationType;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {

    void sendNotification(User recipient, String title, String message, NotificationType type, String referenceId, String referenceType);

    Page<NotificationResponse> getUserNotifications(UserPrincipal user, int page, int size);

    void markAsRead(UUID notificationId, UserPrincipal user);

    void markAllAsRead(UserPrincipal user);

    long getUnreadCount(UserPrincipal user);
}
