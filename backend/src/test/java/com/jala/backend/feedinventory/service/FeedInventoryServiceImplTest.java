package com.jala.backend.feedinventory.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedinventory.dto.response.FeedInventoryResponse;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.mapper.FeedInventoryMapper;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.notification.service.NotificationService;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedInventoryServiceImplTest {

    @Mock
    private FeedInventoryRepository repository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FeedInventoryMapper mapper;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private FeedInventoryServiceImpl service;

    private Site site;
    private FeedInventory inventory;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID())
                .siteCode("S-001")
                .siteName("Site 1")
                .build();

        inventory = FeedInventory.builder()
                .id(UUID.randomUUID())
                .site(site)
                .totalReceivedKg(new BigDecimal("1000"))
                .totalConsumedKg(new BigDecimal("200"))
                .availableKg(new BigDecimal("800"))
                .build();
    }

    @Test
    @DisplayName("getInventoryBySite checks access and maps")
    void getInventoryBySite_found() {
        when(repository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));
        when(mapper.toResponse(inventory))
                .thenReturn(FeedInventoryResponse.builder().build());

        service.getInventoryBySite(site.getId());

        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getInventoryBySite rejects a site with no inventory")
    void getInventoryBySite_notFound() {
        when(repository.findBySiteId(site.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getInventoryBySite(site.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Feed inventory not found.");
    }

    @Nested
    class GetAll {

        @Test
        @DisplayName("unrestricted user reads all inventories")
        void unrestricted() {
            when(siteAccessService.accessibleSiteIds()).thenReturn(null);
            when(repository.findAllByOrderBySiteSiteCode())
                    .thenReturn(List.of(inventory));
            when(mapper.toResponse(inventory))
                    .thenReturn(FeedInventoryResponse.builder().build());

            assertThat(service.getAllInventories()).hasSize(1);
        }

        @Test
        @DisplayName("no assigned sites yields an empty list")
        void none() {
            when(siteAccessService.accessibleSiteIds()).thenReturn(List.of());

            assertThat(service.getAllInventories()).isEmpty();
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("restricted user reads only assigned-site inventories")
        void restricted() {
            List<UUID> ids = List.of(site.getId());
            when(siteAccessService.accessibleSiteIds()).thenReturn(ids);
            when(repository.findBySiteIdInOrderBySiteSiteCode(ids))
                    .thenReturn(List.of(inventory));
            when(mapper.toResponse(inventory))
                    .thenReturn(FeedInventoryResponse.builder().build());

            assertThat(service.getAllInventories()).hasSize(1);
            verify(repository, never()).findAllByOrderBySiteSiteCode();
        }
    }

    @Test
    @DisplayName("increaseInventory adds to received and available")
    void increaseInventory() {
        when(repository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));

        service.increaseInventory(site.getId(), new BigDecimal("100"));

        assertThat(inventory.getTotalReceivedKg()).isEqualByComparingTo("1100");
        assertThat(inventory.getAvailableKg()).isEqualByComparingTo("900");
        verify(repository).save(inventory);
    }

    @Test
    @DisplayName("decreaseInventory consumes stock and stays silent above threshold")
    void decreaseInventory_aboveThreshold() {
        when(repository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));

        service.decreaseInventory(site.getId(), new BigDecimal("100"));

        assertThat(inventory.getTotalConsumedKg()).isEqualByComparingTo("300");
        assertThat(inventory.getAvailableKg()).isEqualByComparingTo("700");
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("decreaseInventory raises a low-stock notification at/below 150kg")
    void decreaseInventory_lowStockNotifies() {
        inventory.setAvailableKg(new BigDecimal("200"));
        when(repository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));

        service.decreaseInventory(site.getId(), new BigDecimal("100"));

        assertThat(inventory.getAvailableKg()).isEqualByComparingTo("100");
        verify(notificationService).createInventoryNotification(
                eq(site.getId()), eq("S-001"), any(), any());
    }

    @Test
    @DisplayName("decreaseInventory rejects an overdraw")
    void decreaseInventory_insufficient() {
        when(repository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));

        assertThatThrownBy(() ->
                service.decreaseInventory(site.getId(), new BigDecimal("9999")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient feed inventory.");

        verify(repository, never()).save(any());
    }
}
