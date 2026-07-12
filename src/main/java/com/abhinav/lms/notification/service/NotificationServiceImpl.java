package com.abhinav.lms.notification.service;

import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.notification.dto.NotificationResponse;
import com.abhinav.lms.notification.entity.Notification;
import com.abhinav.lms.notification.entity.NotificationType;
import com.abhinav.lms.notification.mapper.NotificationMapper;
import com.abhinav.lms.notification.repository.NotificationRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void sendNotification(User recipient, String title, String message, NotificationType type, String referenceId, String referenceType) {
        log.info("Creating notification for recipient: {}, title: {}", recipient.getEmail(), title);
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

        // Broadcast notification to WebSocket queue /user/{email}/queue/notifications
        try {
            log.debug("Broadcasting notification to user queue for: {}", recipient.getEmail());
            messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", response);
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket notification for user: {}", recipient.getEmail(), e);
            // Graceful degradation: notification is already stored in DB and accessible via REST API
        }
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(UserPrincipal user, int page, int size) {
        log.debug("Fetching notification history for user: {}", user.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByRecipientId(user.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UserPrincipal user) {
        log.info("Marking notification ID: {} as read for user: {}", notificationId, user.getUsername());
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to modify this notification");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UserPrincipal user) {
        log.info("Marking all notifications as read for user: {}", user.getUsername());
        Pageable pageable = PageRequest.of(0, 1000);
        List<Notification> unreadList = notificationRepository.findByRecipientId(user.getId(), pageable)
                .getContent();

        LocalDateTime now = LocalDateTime.now();
        unreadList.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(now);
            }
        });
        notificationRepository.saveAll(unreadList);
    }

    @Override
    public long getUnreadCount(UserPrincipal user) {
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }
}
