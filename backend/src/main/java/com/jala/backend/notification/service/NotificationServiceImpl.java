package com.jala.backend.notification.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.common.util.PageRequestUtil;
import com.jala.backend.notification.dto.response.NotificationResponse;
import com.jala.backend.notification.dto.response.NotificationSummaryResponse;
import com.jala.backend.notification.entity.Notification;
import com.jala.backend.notification.enums.NotificationStatus;
import com.jala.backend.notification.enums.NotificationType;
import com.jala.backend.notification.repository.NotificationRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final SiteAccessService siteAccessService;

    @Override
    @Transactional
    public void createFeedNotification(
            UUID siteId,
            UUID pondId,
            String siteCode,
            String pondCode,
            Integer sessionNumber,
            String quantity) {

        log.info(
                "Creating feed notification for pond {}",
                pondCode);

        Notification notification =
                Notification.builder()
                        .type(NotificationType.FEED)
                        .title("Feed Added")
                        .message(
                                String.format(
                                        "%s - %s | Session %d | %s KG added",
                                        siteCode,
                                        pondCode,
                                        sessionNumber,
                                        quantity))
                        .siteId(siteId)
                        .pondId(pondId)
                        .createdAt(DateTimeUtil.now())
                        .build();

        notificationRepository.save(notification);

        log.info("Feed notification created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotifications(
            Integer page,
            Integer size) {

        log.info("Fetching notifications");

        Pageable pageable = PageRequestUtil.of(page, size);

        List<UUID> accessibleSiteIds =
                siteAccessService.accessibleSiteIds();

        List<Notification> notifications;

        long unreadCount;

        if (accessibleSiteIds == null) {

            notifications = notificationRepository
                    .findAllByOrderByCreatedAtDesc(pageable);

            unreadCount = notificationRepository
                    .countByStatus(NotificationStatus.UNREAD);

        } else if (accessibleSiteIds.isEmpty()) {

            notifications = List.of();

            unreadCount = 0;

        } else {

            notifications = notificationRepository
                    .findBySiteIdInOrderByCreatedAtDesc(
                            accessibleSiteIds, pageable);

            unreadCount = notificationRepository
                    .countBySiteIdInAndStatus(
                            accessibleSiteIds,
                            NotificationStatus.UNREAD);
        }

        List<NotificationResponse> responses =
                notifications.stream()
                        .map(this::toResponse)
                        .toList();

        return NotificationSummaryResponse.builder()
                .unreadCount(unreadCount)
                .notifications(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount() {

        List<UUID> accessibleSiteIds =
                siteAccessService.accessibleSiteIds();

        if (accessibleSiteIds == null) {
            return notificationRepository
                    .countByStatus(NotificationStatus.UNREAD);
        }

        if (accessibleSiteIds.isEmpty()) {
            return 0L;
        }

        return notificationRepository
                .countBySiteIdInAndStatus(
                        accessibleSiteIds,
                        NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public void markAsRead(
            UUID notificationId) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found."));

        // Object-level check: users may only mark notifications of
        // sites they can access.
        siteAccessService.checkSiteAccess(notification.getSiteId());

        notification.setStatus(NotificationStatus.READ);

        notification.setReadAt(DateTimeUtil.now());

        notificationRepository.save(notification);

        log.info(
                "Notification {} marked as read",
                notificationId);
    }

    @Override
    @Transactional
    public void createInventoryNotification(
            UUID siteId,
            String siteCode,
            BigDecimal availableKg,
            BigDecimal threshold) {

        boolean alreadyExists =
                notificationRepository
                        .existsByTypeAndSiteIdAndStatus(
                                NotificationType.INVENTORY,
                                siteId,
                                NotificationStatus.UNREAD);

        if (alreadyExists) {

            log.info(
                    "Unread inventory notification already exists for site {}",
                    siteCode);

            return;
        }

        Notification notification =
                Notification.builder()
                        .type(NotificationType.INVENTORY)
                        .title("Low Feed Inventory")
                        .message(
                                String.format(
                                        "%s feed inventory is low. Remaining: %s KG (Threshold: %s KG). Please arrange a new delivery.",
                                        siteCode,
                                        availableKg,
                                        threshold))
                        .siteId(siteId)
                        .status(NotificationStatus.UNREAD)
                        .createdAt(DateTimeUtil.now())
                        .build();

        notificationRepository.save(notification);

        log.info(
                "Inventory notification created for site {}",
                siteCode);
    }

    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .siteId(notification.getSiteId())
                .pondId(notification.getPondId())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
