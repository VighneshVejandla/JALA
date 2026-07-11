package com.jala.backend.notification.dto.response;

import com.jala.backend.notification.enums.NotificationStatus;
import com.jala.backend.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID id;

    private NotificationType type;

    private String title;

    private String message;

    private UUID siteId;

    private UUID pondId;

    private NotificationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}