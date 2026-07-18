package com.jala.backend.analytics.service;

import com.jala.backend.analytics.dto.response.InventoryAnalyticsResponse;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feeddelivery.repository.SiteDeliveryRepository;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private PondRepository pondRepository;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private FeedInventoryRepository feedInventoryRepository;

    @Mock
    private SiteDeliveryRepository siteDeliveryRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private AnalyticsServiceImpl service;

    private Site site;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").siteName("Site 1").build();
    }

    @Test
    @DisplayName("getPondFeedAnalytics checks access and rejects an unknown pond")
    void getPondFeedAnalytics_pondNotFound() {
        UUID pondId = UUID.randomUUID();
        when(pondRepository.findById(pondId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPondFeedAnalytics(pondId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found.");

        verify(siteAccessService).checkPondAccess(pondId);
    }

    @Test
    @DisplayName("getInventoryAnalytics converts available kg into 25kg bags")
    void getInventoryAnalytics_computesBags() {
        FeedInventory inventory = FeedInventory.builder()
                .id(UUID.randomUUID())
                .site(site)
                .availableKg(new BigDecimal("100"))
                .totalReceivedKg(new BigDecimal("500"))
                .totalConsumedKg(new BigDecimal("400"))
                .build();

        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(feedInventoryRepository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));

        when(siteDeliveryRepository.getDeliveredForDate(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(siteDeliveryRepository.getDeliveredBetweenDates(
                eq(site.getId()), any(), any())).thenReturn(BigDecimal.ZERO);
        when(siteDeliveryRepository.getTotalDelivered(site.getId()))
                .thenReturn(new BigDecimal("500"));
        when(feedEntryRepository.getConsumedForDate(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(feedEntryRepository.getConsumedBetweenDates(
                eq(site.getId()), any(), any())).thenReturn(BigDecimal.ZERO);
        when(feedEntryRepository.getTotalConsumed(site.getId()))
                .thenReturn(new BigDecimal("400"));

        InventoryAnalyticsResponse result =
                service.getInventoryAnalytics(site.getId());

        assertThat(result.getAvailableKg()).isEqualByComparingTo("100");
        // 100kg / 25kg per bag = 4 bags
        assertThat(result.getAvailableBags()).isEqualTo(4);
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getInventoryAnalytics rejects a site with no inventory")
    void getInventoryAnalytics_inventoryNotFound() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(feedInventoryRepository.findBySiteId(site.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getInventoryAnalytics(site.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getSiteFeedAnalytics rejects an unknown site")
    void getSiteFeedAnalytics_siteNotFound() {
        UUID siteId = UUID.randomUUID();
        when(siteRepository.findById(siteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSiteFeedAnalytics(siteId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(siteAccessService).checkSiteAccess(siteId);
    }

    @Test
    @DisplayName("getSiteFeedAnalytics aggregates feed metrics for the site")
    void getSiteFeedAnalytics_success() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(feedEntryRepository.getSiteFeedForDate(eq(site.getId()), any()))
                .thenReturn(new BigDecimal("10"));
        when(feedEntryRepository.getSiteFeedBetweenDates(
                eq(site.getId()), any(), any())).thenReturn(new BigDecimal("70"));
        when(feedEntryRepository.countSiteEntriesForDate(eq(site.getId()), any()))
                .thenReturn(2);
        when(feedEntryRepository.countSiteEntriesBetweenDates(
                eq(site.getId()), any(), any())).thenReturn(14);
        when(feedEntryRepository.countPondsFedForDate(eq(site.getId()), any()))
                .thenReturn(1);
        when(feedEntryRepository.countPondsFedBetweenDates(
                eq(site.getId()), any(), any())).thenReturn(5);

        var result = service.getSiteFeedAnalytics(site.getId());

        assertThat(result.getTodayFeedKg()).isEqualByComparingTo("10");
        assertThat(result.getSiteCode()).isEqualTo("S-001");
    }

    @Test
    @DisplayName("getPondHarvestAnalytics aggregates pond harvest metrics")
    void getPondHarvestAnalytics_success() {
        com.jala.backend.pond.entity.Pond pond =
                com.jala.backend.pond.entity.Pond.builder()
                        .id(UUID.randomUUID()).site(site)
                        .pondCode("P-001").pondName("Pond 1").build();

        when(pondRepository.findById(pond.getId())).thenReturn(Optional.of(pond));
        when(harvestRepository.getHarvestCountByPond(pond.getId())).thenReturn(2);
        when(harvestRepository.getTotalHarvestKg(pond.getId()))
                .thenReturn(new BigDecimal("400"));
        when(harvestRepository.getAverageHarvestKg(pond.getId()))
                .thenReturn(new BigDecimal("200"));
        when(harvestRepository.getTotalRevenue(pond.getId()))
                .thenReturn(new BigDecimal("4600"));
        when(harvestRepository
                .findFirstByPondCyclePondIdAndStatusOrderByHarvestDateDescUploadedAtDesc(
                        eq(pond.getId()), any()))
                .thenReturn(Optional.empty());

        var result = service.getPondHarvestAnalytics(pond.getId());

        assertThat(result.getHarvestCount()).isEqualTo(2);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo("4600");
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getSiteHarvestAnalytics aggregates site harvest windows")
    void getSiteHarvestAnalytics_success() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(harvestRepository.getHarvestCountBySite(site.getId())).thenReturn(5);
        when(harvestRepository.getSiteHarvestBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(new BigDecimal("120"));
        when(harvestRepository.getSiteRevenueBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(new BigDecimal("1500"));

        var result = service.getSiteHarvestAnalytics(site.getId());

        assertThat(result.getHarvestCount()).isEqualTo(5);
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getAnalyticsDashboard composes feed, inventory and harvest sections")
    void getAnalyticsDashboard_success() {
        // The dashboard delegates to sibling methods via the injected self
        // proxy; in a unit test point it back at the instance under test.
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "self", service);

        com.jala.backend.feedinventory.entity.FeedInventory inv =
                com.jala.backend.feedinventory.entity.FeedInventory.builder()
                        .id(UUID.randomUUID()).site(site)
                        .availableKg(new BigDecimal("100"))
                        .totalReceivedKg(new BigDecimal("500"))
                        .totalConsumedKg(new BigDecimal("400")).build();

        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(feedInventoryRepository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inv));
        // feed section
        when(feedEntryRepository.getSiteFeedForDate(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(feedEntryRepository.getSiteFeedBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(feedEntryRepository.countSiteEntriesForDate(eq(site.getId()), any()))
                .thenReturn(0);
        when(feedEntryRepository.countSiteEntriesBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(0);
        when(feedEntryRepository.countPondsFedForDate(eq(site.getId()), any()))
                .thenReturn(0);
        when(feedEntryRepository.countPondsFedBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(0);
        // inventory section
        when(siteDeliveryRepository.getDeliveredForDate(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(siteDeliveryRepository.getDeliveredBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(siteDeliveryRepository.getTotalDelivered(site.getId()))
                .thenReturn(new BigDecimal("500"));
        when(feedEntryRepository.getConsumedForDate(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(feedEntryRepository.getConsumedBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(feedEntryRepository.getTotalConsumed(site.getId()))
                .thenReturn(new BigDecimal("400"));
        // harvest section
        when(harvestRepository.getHarvestCountBySite(site.getId())).thenReturn(1);
        when(harvestRepository.getSiteHarvestBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(harvestRepository.getSiteRevenueBetweenDates(eq(site.getId()), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        var result = service.getAnalyticsDashboard(site.getId());

        assertThat(result.getSiteCode()).isEqualTo("S-001");
        assertThat(result.getFeed()).isNotNull();
        assertThat(result.getInventory()).isNotNull();
        assertThat(result.getHarvest()).isNotNull();
    }
}
