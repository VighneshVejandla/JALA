package com.jala.backend.notification.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.notification.dto.response.NotificationSummaryResponse;
import com.jala.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.NOTIFICATION_BASE_URL)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>>
    getNotifications() {

        NotificationSummaryResponse response =
                notificationService.getNotifications();

        return ResponseEntity.ok(
                ApiResponse.<NotificationSummaryResponse>builder()
                        .success(true)
                        .message("Notifications fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<Void>>
    markAsRead(
            @PathVariable
            UUID notificationId) {

        notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Notification marked as read")
                        .build()
        );
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<Long>>
    getUnreadCount() {

        Long unreadCount =
                notificationService.getUnreadCount();

        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .success(true)
                        .message("Unread notification count fetched successfully")
                        .data(unreadCount)
                        .build()
        );
    }
}