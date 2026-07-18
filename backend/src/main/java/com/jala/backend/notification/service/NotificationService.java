package com.jala.backend.notification.service;

import com.jala.backend.notification.dto.response.NotificationSummaryResponse;

import java.util.UUID;

public interface NotificationService {

    void createFeedNotification(
            UUID siteId,
            UUID pondId,
            String siteCode,
            String pondCode,
            Integer sessionNumber,
            String quantity);

    NotificationSummaryResponse getNotifications(
            Integer page,
            Integer size);

    void markAsRead(
            UUID notificationId);

    Long getUnreadCount();

    void createInventoryNotification(
            UUID siteId,
            String siteCode,
            java.math.BigDecimal availableKg,
            java.math.BigDecimal threshold);
}
