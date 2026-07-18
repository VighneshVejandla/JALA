package com.jala.backend.dashboard.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.dashboard.dto.response.HomeDashboardResponse;
import com.jala.backend.dashboard.dto.response.PondDashboardResponse;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.harvest.enums.HarvestStatus;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.notification.enums.NotificationStatus;
import com.jala.backend.notification.repository.NotificationRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PondDashboardServiceImplTest {

    @Mock
    private PondRepository pondRepository;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicinePhotoRepository medicinePhotoRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private FeedInventoryRepository feedInventoryRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private PondDashboardServiceImpl service;

    private Site site;
    private Pond pond;
    private PondCycle cycle;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").siteName("Site 1").build();
        pond = Pond.builder()
                .id(UUID.randomUUID()).site(site)
                .pondCode("P-001").pondName("Pond 1").build();
        cycle = PondCycle.builder()
                .id(UUID.randomUUID()).pond(pond).cycleNumber(1)
                .status(PondCycleStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("getDashboard aggregates the active cycle's metrics")
    void getDashboard_success() {
        when(pondRepository.findById(pond.getId())).thenReturn(Optional.of(pond));
        when(pondCycleRepository.findByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(Optional.of(cycle));

        when(feedEntryRepository.getTodayFeedKg(eq(cycle.getId()), any()))
                .thenReturn(new BigDecimal("10"));
        when(feedEntryRepository.getTotalFeedKg(cycle.getId()))
                .thenReturn(new BigDecimal("100"));
        when(feedEntryRepository.countByPondCycleIdAndFeedDateAndStatus(
                eq(cycle.getId()), any(), eq(FeedEntryStatus.ACTIVE))).thenReturn(2);
        when(medicineRepository.getMedicineEntryCount(cycle.getId())).thenReturn(3);
        when(medicineRepository.getTotalMedicineQuantity(cycle.getId()))
                .thenReturn(new BigDecimal("5"));
        when(medicineRepository.getLastMedicineDate(cycle.getId())).thenReturn(null);
        when(medicinePhotoRepository.countByCycleWithActiveEntries(cycle.getId()))
                .thenReturn(4L);
        when(harvestRepository.getHarvestCount(pond.getId())).thenReturn(0);
        when(harvestRepository
                .findFirstByPondCyclePondIdAndStatusOrderByHarvestDateDescUploadedAtDesc(
                        pond.getId(), HarvestStatus.ACTIVE))
                .thenReturn(Optional.empty());

        PondDashboardResponse result = service.getDashboard(pond.getId());

        assertThat(result.getPondCode()).isEqualTo("P-001");
        assertThat(result.getMedicinePhotoCount()).isEqualTo(4);
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getDashboard rejects a pond with no active cycle")
    void getDashboard_noActiveCycle() {
        when(pondRepository.findById(pond.getId())).thenReturn(Optional.of(pond));
        when(pondCycleRepository.findByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDashboard(pond.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No active cycle found.");
    }

    @Test
    @DisplayName("getHomeDashboard flags low inventory at or below threshold")
    void getHomeDashboard_lowInventory() {
        FeedInventory inventory = FeedInventory.builder()
                .id(UUID.randomUUID()).site(site)
                .availableKg(new BigDecimal("100")).build();

        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(feedInventoryRepository.findBySiteId(site.getId()))
                .thenReturn(Optional.of(inventory));
        when(pondRepository.countBySiteId(site.getId())).thenReturn(5L);
        when(pondCycleRepository.countByPondSiteIdAndStatus(
                site.getId(), PondCycleStatus.ACTIVE)).thenReturn(3L);
        when(feedEntryRepository.getSiteFeedForDate(eq(site.getId()), any()))
                .thenReturn(new BigDecimal("20"));
        when(harvestRepository.getTodayHarvestKg(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(harvestRepository.getTodayRevenue(eq(site.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(notificationRepository.countBySiteIdAndStatus(
                site.getId(), NotificationStatus.UNREAD)).thenReturn(1L);

        HomeDashboardResponse result = service.getHomeDashboard(site.getId());

        assertThat(result.getLowInventory()).isTrue();
        assertThat(result.getTotalPonds()).isEqualTo(5L);
        verify(siteAccessService).checkSiteAccess(site.getId());
    }
}
