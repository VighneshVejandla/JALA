package com.jala.backend.notification.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.notification.dto.response.NotificationSummaryResponse;
import com.jala.backend.notification.entity.Notification;
import com.jala.backend.notification.enums.NotificationStatus;
import com.jala.backend.notification.repository.NotificationRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private NotificationServiceImpl service;

    private Notification notification;
    private UUID siteId;

    @BeforeEach
    void setUp() {
        siteId = UUID.randomUUID();
        notification = Notification.builder()
                .id(UUID.randomUUID())
                .siteId(siteId)
                .status(NotificationStatus.UNREAD)
                .build();
    }

    @Test
    @DisplayName("unrestricted user sees all notifications and the global unread count")
    void getNotifications_unrestricted() {
        when(siteAccessService.accessibleSiteIds()).thenReturn(null);
        when(notificationRepository.findAllByOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(notification));
        when(notificationRepository.countByStatus(NotificationStatus.UNREAD))
                .thenReturn(3L);

        NotificationSummaryResponse summary =
                service.getNotifications(null, null);

        assertThat(summary.getNotifications()).hasSize(1);
        assertThat(summary.getUnreadCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("user with no assigned sites sees nothing")
    void getNotifications_noSites() {
        when(siteAccessService.accessibleSiteIds()).thenReturn(List.of());

        NotificationSummaryResponse summary =
                service.getNotifications(null, null);

        assertThat(summary.getNotifications()).isEmpty();
        assertThat(summary.getUnreadCount()).isZero();
        verify(notificationRepository, never())
                .findAllByOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("restricted user sees only assigned-site notifications")
    void getNotifications_restricted() {
        List<UUID> ids = List.of(siteId);
        when(siteAccessService.accessibleSiteIds()).thenReturn(ids);
        when(notificationRepository
                .findBySiteIdInOrderByCreatedAtDesc(eq(ids), any()))
                .thenReturn(List.of(notification));
        when(notificationRepository.countBySiteIdInAndStatus(
                eq(ids), eq(NotificationStatus.UNREAD))).thenReturn(1L);

        NotificationSummaryResponse summary =
                service.getNotifications(0, 20);

        assertThat(summary.getNotifications()).hasSize(1);
        assertThat(summary.getUnreadCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUnreadCount is site-scoped for restricted users")
    void getUnreadCount_restricted() {
        List<UUID> ids = List.of(siteId);
        when(siteAccessService.accessibleSiteIds()).thenReturn(ids);
        when(notificationRepository.countBySiteIdInAndStatus(
                eq(ids), eq(NotificationStatus.UNREAD))).thenReturn(7L);

        assertThat(service.getUnreadCount()).isEqualTo(7L);
    }

    @Test
    @DisplayName("markAsRead enforces site access then flips the status")
    void markAsRead_success() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        service.markAsRead(notification.getId());

        verify(siteAccessService).checkSiteAccess(siteId);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead rejects an unknown notification")
    void markAsRead_notFound() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found.");
    }

    @Test
    @DisplayName("createInventoryNotification is suppressed when one is already unread")
    void createInventoryNotification_deduplicated() {
        when(notificationRepository.existsByTypeAndSiteIdAndStatus(
                any(), any(), any())).thenReturn(true);

        service.createInventoryNotification(
                siteId, "S-001",
                java.math.BigDecimal.TEN, java.math.BigDecimal.valueOf(150));

        verify(notificationRepository, never()).save(any());
    }
}
