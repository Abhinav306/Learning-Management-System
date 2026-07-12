package com.abhinav.lms.notification.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.notification.dto.NotificationResponse;
import com.abhinav.lms.notification.service.NotificationService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notification Management", description = "Endpoints for managing user-specific in-app notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get paginated notification history for the current authenticated user")
    public ApiResponse<PagedResponse<NotificationResponse>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(principal, page, size);
        return ApiResponse.success(PagedResponse.from(notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get total unread notifications count for the current authenticated user")
    public ApiResponse<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal);
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a specific notification as read by ID")
    public ApiResponse<Void> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal);
        return ApiResponse.success(null, "Notification marked as read");
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications for the current user as read")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal);
        return ApiResponse.success(null, "All notifications marked as read");
    }
}
