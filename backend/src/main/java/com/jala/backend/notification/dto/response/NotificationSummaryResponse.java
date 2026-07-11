package com.jala.backend.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationSummaryResponse {

    private Long unreadCount;

    private List<NotificationResponse> notifications;
}