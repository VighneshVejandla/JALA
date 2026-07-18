package com.jala.backend.reports.service;

import com.jala.backend.common.constants.MessageConstants;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.reports.dto.request.ReportFilterRequest;
import com.jala.backend.reports.dto.response.*;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportsServiceImpl
        implements ReportsService {

    private final HarvestRepository harvestRepository;

    private final SiteRepository siteRepository;

    private final PondRepository pondRepository;

    private final FeedEntryRepository feedEntryRepository;

    private final MedicineRepository medicineRepository;

    private final MedicinePhotoRepository medicinePhotoRepository;

    private final SiteAccessService siteAccessService;

    // Self-reference (proxy) so getDashboard's calls to the other report
    // methods go through the transactional proxy. @Lazy breaks the
    // constructor cycle.
    @Lazy
    @Autowired
    private ReportsService self;


    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(
            ReportFilterRequest request) {

        log.info(
                "Generating revenue report for site {}",
                request.getSiteId());

        siteAccessService.checkSiteAccess(request.getSiteId());

        Site site = getSiteOrThrow(request.getSiteId());

        Pond pond = resolvePond(request.getPondId());

        List<Harvest> harvests =
                harvestRepository.findRevenueReport(
                        request.getSiteId(),
                        request.getPondId(),
                        request.getFromDate(),
                        request.getToDate());

        int harvestCount = harvests.size();

        BigDecimal totalHarvestKg =
                sum(harvests, Harvest::getHarvestQuantityKg);

        BigDecimal totalRevenue =
                sum(harvests, Harvest::getTotalAmount);

        BigDecimal totalSellingPrice =
                sum(harvests, Harvest::getSellingPricePerKg);

        BigDecimal averageHarvestKg =
                average(totalHarvestKg, harvestCount);

        BigDecimal averageSellingPrice =
                average(totalSellingPrice, harvestCount);

        return RevenueReportResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .pondId(pond != null ? pond.getId() : null)
                .pondCode(pond != null ? pond.getPondCode() : null)
                .pondName(pond != null ? pond.getPondName() : null)

                .fromDate(request.getFromDate())
                .toDate(request.getToDate())

                .harvestCount(harvestCount)

                .totalHarvestKg(totalHarvestKg)

                .averageHarvestKg(averageHarvestKg)

                .averageSellingPrice(averageSellingPrice)

                .totalRevenue(totalRevenue)

                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedReportResponse getFeedReport(
            ReportFilterRequest request) {

        log.info(
                "Generating feed report for site {}",
                request.getSiteId());

        siteAccessService.checkSiteAccess(request.getSiteId());

        Site site = getSiteOrThrow(request.getSiteId());

        Pond pond = resolvePond(request.getPondId());

        List<FeedEntry> entries =
                feedEntryRepository.findFeedReport(
                        request.getSiteId(),
                        request.getPondId(),
                        request.getFromDate(),
                        request.getToDate());

        int count = entries.size();

        BigDecimal totalFeedKg =
                sum(entries, FeedEntry::getFeedQuantityKg);

        BigDecimal averageFeed = average(totalFeedKg, count);

        List<FeedReportItemResponse> details =
                entries.stream()
                        .map(entry ->
                                FeedReportItemResponse.builder()
                                        .feedDate(entry.getFeedDate())
                                        .cycleNumber(entry.getPondCycle().getCycleNumber())
                                        .sessionNumber(entry.getFeedSchedule().getSessionNumber())
                                        .feedSize(entry.getFeedSize().name())
                                        .feedQuantityKg(entry.getFeedQuantityKg())
                                        .remarks(entry.getRemarks())
                                        .createdBy(entry.getCreatedBy().getEmployeeCode())
                                        .build())
                        .toList();

        return FeedReportResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .pondId(pond != null ? pond.getId() : null)
                .pondCode(pond != null ? pond.getPondCode() : null)
                .pondName(pond != null ? pond.getPondName() : null)

                .fromDate(request.getFromDate())
                .toDate(request.getToDate())

                .feedEntryCount(count)

                .totalFeedKg(totalFeedKg)

                .averageFeedPerEntry(averageFeed)

                .details(details)

                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyChartResponse> getRevenueChart(UUID siteId) {

        siteAccessService.checkSiteAccess(siteId);

        return buildMonthlyChart(
                toMonthTotals(harvestRepository.sumRevenueByMonth(siteId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyChartResponse> getFeedChart(UUID siteId) {

        siteAccessService.checkSiteAccess(siteId);

        return buildMonthlyChart(
                toMonthTotals(feedEntryRepository.sumFeedKgByMonth(siteId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyChartResponse> getHarvestChart(UUID siteId) {

        siteAccessService.checkSiteAccess(siteId);

        return buildMonthlyChart(
                toMonthTotals(harvestRepository.sumHarvestKgByMonth(siteId)));
    }

    /** Maps (month, total) aggregate rows to a month-indexed lookup. */
    private Map<Integer, BigDecimal> toMonthTotals(List<Object[]> rows) {

        Map<Integer, BigDecimal> totals = new HashMap<>();

        for (Object[] row : rows) {
            totals.put(
                    ((Number) row[0]).intValue(),
                    (BigDecimal) row[1]);
        }

        return totals;
    }

    private List<MonthlyChartResponse> buildMonthlyChart(
            Map<Integer, BigDecimal> values) {

        List<MonthlyChartResponse> chart =
                new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            chart.add(
                    MonthlyChartResponse.builder()
                            .month(month)
                            .monthName(
                                    Month.of(month)
                                            .name()
                                            .substring(0, 3))
                            .value(
                                    values.getOrDefault(
                                            month,
                                            BigDecimal.ZERO))
                            .build());
        }

        return chart;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportsDashboardResponse getDashboard(
            UUID siteId) {

        log.info(
                "Fetching reports dashboard for site {}",
                siteId);

        siteAccessService.checkSiteAccess(siteId);

        Site site = getSiteOrThrow(siteId);

        ReportFilterRequest filter =
                new ReportFilterRequest();

        filter.setSiteId(siteId);

        filter.setPondId(null);

        filter.setFromDate(
                LocalDate.of(
                        DateTimeUtil.today().getYear(),
                        Month.JANUARY,
                        1));

        filter.setToDate(
                DateTimeUtil.today());

        // Called via the injected proxy (self) so each sub-report runs in
        // its own declared transaction, rather than a self-invocation that
        // would bypass Spring's transactional proxy.
        RevenueReportResponse revenue =
                self.getRevenueReport(filter);

        FeedReportResponse feed =
                self.getFeedReport(filter);

        MedicineReportResponse medicine =
                self.getMedicineReport(filter);

        ChartsResponse charts =
                ChartsResponse.builder()
                        .revenue(
                                self.getRevenueChart(siteId))
                        .feed(
                                self.getFeedChart(siteId))
                        .harvest(
                                self.getHarvestChart(siteId))
                        .build();

        return ReportsDashboardResponse.builder()

                .siteId(site.getId())

                .siteCode(site.getSiteCode())

                .siteName(site.getSiteName())

                .revenue(revenue)

                .feed(feed)

                .medicine(medicine)

                .charts(charts)

                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineReportResponse getMedicineReport(
            ReportFilterRequest request) {

        log.info(
                "Generating medicine report for site {}",
                request.getSiteId());

        siteAccessService.checkSiteAccess(request.getSiteId());

        Site site = getSiteOrThrow(request.getSiteId());

        Pond pond = resolvePond(request.getPondId());

        LocalDateTime fromDateTime =
                request.getFromDate().atStartOfDay();

        // Half-open upper bound: includes every instant of toDate.
        LocalDateTime toDateTimeExclusive =
                request.getToDate().plusDays(1).atStartOfDay();

        List<MedicineEntry> entries =
                medicineRepository.findMedicineReport(
                        request.getSiteId(),
                        request.getPondId(),
                        fromDateTime,
                        toDateTimeExclusive);

        int count = entries.size();

        BigDecimal totalQuantity =
                sum(entries, MedicineEntry::getQuantity);

        Map<UUID, List<MedicinePhoto>> photosByEntry =
                loadPhotosByEntry(entries);

        List<MedicineReportItemResponse> details =
                entries.stream()
                        .map(entry -> MedicineReportItemResponse.builder()
                                .createdAt(entry.getCreatedAt())
                                .cycleNumber(entry.getPondCycle().getCycleNumber())
                                .quantity(entry.getQuantity())
                                .unit(entry.getUnit().name())
                                .remarks(entry.getRemarks())
                                .createdBy(entry.getCreatedBy().getEmployeeCode())
                                .photos(toPhotoResponses(
                                        photosByEntry.getOrDefault(
                                                entry.getId(), List.of())))
                                .build())
                        .toList();

        return MedicineReportResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .pondId(pond != null ? pond.getId() : null)
                .pondCode(pond != null ? pond.getPondCode() : null)
                .pondName(pond != null ? pond.getPondName() : null)

                .fromDate(request.getFromDate())
                .toDate(request.getToDate())

                .medicineEntryCount(count)

                .totalMedicineQuantity(totalQuantity)

                .details(details)

                .build();
    }

    /** One query for all photos of the report instead of one per entry. */
    private Map<UUID, List<MedicinePhoto>> loadPhotosByEntry(
            List<MedicineEntry> entries) {

        if (entries.isEmpty()) {
            return Map.of();
        }

        List<UUID> entryIds = entries.stream()
                .map(MedicineEntry::getId)
                .toList();

        return medicinePhotoRepository
                .findByMedicineEntryIdInOrderByUploadedAt(entryIds)
                .stream()
                .collect(Collectors.groupingBy(
                        photo -> photo.getMedicineEntry().getId()));
    }

    private List<MedicinePhotoReportResponse> toPhotoResponses(
            List<MedicinePhoto> photos) {

        return photos.stream()
                .map(photo -> MedicinePhotoReportResponse.builder()
                        .fileName(photo.getFileName())
                        .filePath(photo.getFilePath())
                        .contentType(photo.getContentType())
                        .fileSize(photo.getFileSize())
                        .build())
                .toList();
    }

    private Site getSiteOrThrow(UUID siteId) {

        return siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.SITE_NOT_FOUND));
    }

    private Pond resolvePond(UUID pondId) {

        if (pondId == null) {
            return null;
        }

        return pondRepository.findById(pondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.POND_NOT_FOUND));
    }

    private static <T> BigDecimal sum(
            List<T> items,
            java.util.function.Function<T, BigDecimal> extractor) {

        return items.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal average(BigDecimal total, int count) {

        return count == 0
                ? BigDecimal.ZERO
                : total.divide(
                        BigDecimal.valueOf(count),
                        2,
                        RoundingMode.HALF_UP);
    }
}
