package com.jala.backend.reports.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.reports.dto.request.ReportFilterRequest;
import com.jala.backend.reports.dto.response.MonthlyChartResponse;
import com.jala.backend.reports.dto.response.RevenueReportResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportsServiceImplTest {

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PondRepository pondRepository;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicinePhotoRepository medicinePhotoRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private ReportsServiceImpl service;

    private Site site;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").siteName("Site 1").build();
    }

    private ReportFilterRequest filter() {
        ReportFilterRequest f = new ReportFilterRequest();
        f.setSiteId(site.getId());
        f.setFromDate(LocalDate.now().minusMonths(1));
        f.setToDate(LocalDate.now());
        return f;
    }

    @Test
    @DisplayName("getRevenueReport totals and averages the harvests")
    void getRevenueReport_success() {
        Harvest h1 = Harvest.builder()
                .id(UUID.randomUUID())
                .harvestQuantityKg(new BigDecimal("100"))
                .totalAmount(new BigDecimal("1000"))
                .sellingPricePerKg(new BigDecimal("10"))
                .build();
        Harvest h2 = Harvest.builder()
                .id(UUID.randomUUID())
                .harvestQuantityKg(new BigDecimal("300"))
                .totalAmount(new BigDecimal("3600"))
                .sellingPricePerKg(new BigDecimal("12"))
                .build();

        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(harvestRepository.findRevenueReport(any(), any(), any(), any()))
                .thenReturn(List.of(h1, h2));

        RevenueReportResponse result = service.getRevenueReport(filter());

        assertThat(result.getHarvestCount()).isEqualTo(2);
        assertThat(result.getTotalHarvestKg()).isEqualByComparingTo("400");
        assertThat(result.getTotalRevenue()).isEqualByComparingTo("4600");
        // average harvest kg = 400 / 2 = 200
        assertThat(result.getAverageHarvestKg()).isEqualByComparingTo("200.00");
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getRevenueReport rejects an unknown site")
    void getRevenueReport_siteNotFound() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRevenueReport(filter()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getRevenueChart maps SQL month aggregates onto a 12-month series")
    void getRevenueChart_success() {
        when(harvestRepository.sumRevenueByMonth(site.getId()))
                .thenReturn(List.of(
                        new Object[]{1, new BigDecimal("500")},
                        new Object[]{3, new BigDecimal("200")}));

        List<MonthlyChartResponse> chart =
                service.getRevenueChart(site.getId());

        assertThat(chart).hasSize(12);
        assertThat(chart.get(0).getMonth()).isEqualTo(1);
        assertThat(chart.get(0).getValue()).isEqualByComparingTo("500");
        assertThat(chart.get(1).getValue()).isEqualByComparingTo("0");
        assertThat(chart.get(2).getValue()).isEqualByComparingTo("200");
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getFeedChart and getHarvestChart also build 12-month series")
    void feedAndHarvestCharts() {
        when(feedEntryRepository.sumFeedKgByMonth(site.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{2, new BigDecimal("80")}));
        when(harvestRepository.sumHarvestKgByMonth(site.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{6, new BigDecimal("900")}));

        assertThat(service.getFeedChart(site.getId())).hasSize(12);
        assertThat(service.getHarvestChart(site.getId())).hasSize(12);
    }

    @Test
    @DisplayName("getFeedReport totals feed and builds per-entry detail rows")
    void getFeedReport_success() {
        com.jala.backend.site.entity.Site s = site;
        com.jala.backend.pond.entity.Pond pond =
                com.jala.backend.pond.entity.Pond.builder()
                        .id(UUID.randomUUID()).site(s).build();
        com.jala.backend.pondcycle.entity.PondCycle cycle =
                com.jala.backend.pondcycle.entity.PondCycle.builder()
                        .id(UUID.randomUUID()).pond(pond).cycleNumber(2).build();
        com.jala.backend.feedschedule.entity.FeedSchedule sch =
                com.jala.backend.feedschedule.entity.FeedSchedule.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).sessionNumber(1).build();
        com.jala.backend.user.entity.User u =
                com.jala.backend.user.entity.User.builder()
                        .id(UUID.randomUUID()).employeeCode("E-1").build();
        com.jala.backend.feedentry.entity.FeedEntry fe =
                com.jala.backend.feedentry.entity.FeedEntry.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).feedSchedule(sch)
                        .createdBy(u)
                        .feedSize(com.jala.backend.feedentry.enums.FeedSize.ONE)
                        .feedDate(LocalDate.now())
                        .feedQuantityKg(new BigDecimal("25"))
                        .build();

        when(siteRepository.findById(s.getId())).thenReturn(Optional.of(s));
        when(feedEntryRepository.findFeedReport(any(), any(), any(), any()))
                .thenReturn(List.of(fe));

        var result = service.getFeedReport(filter());

        assertThat(result.getFeedEntryCount()).isEqualTo(1);
        assertThat(result.getTotalFeedKg()).isEqualByComparingTo("25");
        assertThat(result.getDetails()).hasSize(1);
    }

    @Test
    @DisplayName("getMedicineReport totals quantity and attaches batched photos")
    void getMedicineReport_success() {
        com.jala.backend.pond.entity.Pond pond =
                com.jala.backend.pond.entity.Pond.builder()
                        .id(UUID.randomUUID()).site(site).build();
        com.jala.backend.pondcycle.entity.PondCycle cycle =
                com.jala.backend.pondcycle.entity.PondCycle.builder()
                        .id(UUID.randomUUID()).pond(pond).cycleNumber(1).build();
        com.jala.backend.user.entity.User u =
                com.jala.backend.user.entity.User.builder()
                        .id(UUID.randomUUID()).employeeCode("E-1").build();
        com.jala.backend.medicine.entity.MedicineEntry me =
                com.jala.backend.medicine.entity.MedicineEntry.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).createdBy(u)
                        .quantity(new BigDecimal("3"))
                        .unit(com.jala.backend.medicine.enums.MedicineUnit.ML)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();

        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(medicineRepository.findMedicineReport(any(), any(), any(), any()))
                .thenReturn(List.of(me));
        when(medicinePhotoRepository
                .findByMedicineEntryIdInOrderByUploadedAt(any()))
                .thenReturn(List.of());

        var result = service.getMedicineReport(filter());

        assertThat(result.getMedicineEntryCount()).isEqualTo(1);
        assertThat(result.getTotalMedicineQuantity()).isEqualByComparingTo("3");
        assertThat(result.getDetails()).hasSize(1);
    }

    @Test
    @DisplayName("getDashboard composes the sub-reports/charts via the self proxy")
    void getDashboard_success() {
        // getDashboard delegates to the sibling methods through the injected
        // self proxy; point it at a mock so this test covers the composition.
        ReportsService selfProxy =
                org.mockito.Mockito.mock(ReportsService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "self", selfProxy);

        when(siteRepository.findById(site.getId()))
                .thenReturn(Optional.of(site));
        when(selfProxy.getRevenueReport(any()))
                .thenReturn(RevenueReportResponse.builder().build());
        when(selfProxy.getFeedReport(any()))
                .thenReturn(com.jala.backend.reports.dto.response
                        .FeedReportResponse.builder().build());
        when(selfProxy.getMedicineReport(any()))
                .thenReturn(com.jala.backend.reports.dto.response
                        .MedicineReportResponse.builder().build());
        when(selfProxy.getRevenueChart(site.getId())).thenReturn(List.of());
        when(selfProxy.getFeedChart(site.getId())).thenReturn(List.of());
        when(selfProxy.getHarvestChart(site.getId())).thenReturn(List.of());

        var result = service.getDashboard(site.getId());

        assertThat(result.getSiteCode()).isEqualTo("S-001");
        assertThat(result.getCharts()).isNotNull();
        verify(siteAccessService).checkSiteAccess(site.getId());
    }
}
