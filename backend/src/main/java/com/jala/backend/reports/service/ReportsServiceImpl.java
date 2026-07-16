package com.jala.backend.reports.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.reports.dto.request.ReportFilterRequest;
import com.jala.backend.reports.dto.response.*;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private static final String SITE_NOT_FOUND = "Site not found.";

    private static final String POND_NOT_FOUND = "Pond not found.";

    @Override
    public RevenueReportResponse getRevenueReport(
            ReportFilterRequest request) {

        log.info(
                "Generating revenue report for site {}",
                request.getSiteId());

        Site site = siteRepository.findById(
                        request.getSiteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(SITE_NOT_FOUND));

        Pond pond = null;

        if (request.getPondId() != null) {

            pond = pondRepository.findById(
                            request.getPondId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(POND_NOT_FOUND));
        }

        List<Harvest> harvests =
                harvestRepository.findRevenueReport(
                        request.getSiteId(),
                        request.getPondId(),
                        request.getFromDate(),
                        request.getToDate());

        int harvestCount = harvests.size();

        BigDecimal totalHarvestKg =
                harvests.stream()
                        .map(Harvest::getHarvestQuantityKg)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        BigDecimal totalRevenue =
                harvests.stream()
                        .map(Harvest::getTotalAmount)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        BigDecimal totalSellingPrice =
                harvests.stream()
                        .map(Harvest::getSellingPricePerKg)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        BigDecimal averageHarvestKg =
                BigDecimal.ZERO;

        BigDecimal averageSellingPrice =
                BigDecimal.ZERO;

        if (harvestCount > 0) {

            averageHarvestKg =
                    totalHarvestKg.divide(
                            BigDecimal.valueOf(harvestCount),
                            2,
                            RoundingMode.HALF_UP);

            averageSellingPrice =
                    totalSellingPrice.divide(
                            BigDecimal.valueOf(harvestCount),
                            2,
                            RoundingMode.HALF_UP);
        }

        return RevenueReportResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .pondId(
                        pond != null
                                ? pond.getId()
                                : null)

                .pondCode(
                        pond != null
                                ? pond.getPondCode()
                                : null)

                .pondName(
                        pond != null
                                ? pond.getPondName()
                                : null)

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
    public FeedReportResponse getFeedReport(
            ReportFilterRequest request) {

        log.info(
                "Generating feed report for site {}",
                request.getSiteId());

        Site site = siteRepository.findById(
                        request.getSiteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(SITE_NOT_FOUND));

        Pond pond = null;

        if (request.getPondId() != null) {

            pond = pondRepository.findById(
                            request.getPondId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(POND_NOT_FOUND));
        }

        List<FeedEntry> entries =
                feedEntryRepository.findFeedReport(
                        request.getSiteId(),
                        request.getPondId(),
                        request.getFromDate(),
                        request.getToDate());

        int count = entries.size();

        BigDecimal totalFeedKg =
                entries.stream()
                        .map(FeedEntry::getFeedQuantityKg)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        BigDecimal averageFeed =
                count == 0
                        ? BigDecimal.ZERO
                        : totalFeedKg.divide(
                        BigDecimal.valueOf(count),
                        2,
                        RoundingMode.HALF_UP);

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

                .pondId(
                        pond != null
                                ? pond.getId()
                                : null)

                .pondCode(
                        pond != null
                                ? pond.getPondCode()
                                : null)

                .pondName(
                        pond != null
                                ? pond.getPondName()
                                : null)

                .fromDate(request.getFromDate())
                .toDate(request.getToDate())

                .feedEntryCount(count)

                .totalFeedKg(totalFeedKg)

                .averageFeedPerEntry(averageFeed)

                .details(details)

                .build();
    }

    @Override
    public List<MonthlyChartResponse> getRevenueChart(UUID siteId) {
        Map<Integer, BigDecimal> values =
                new java.util.HashMap<>();

        List<Harvest> harvests =
                harvestRepository.findAllBySiteForChart(siteId);

        for (Harvest harvest : harvests) {

            if (harvest.getHarvestDate() == null
                    || harvest.getTotalAmount() == null) {
                continue;
            }

            int month =
                    harvest.getHarvestDate()
                            .getMonthValue();

            values.merge(
                    month,
                    harvest.getTotalAmount(),
                    BigDecimal::add);
        }

        return buildMonthlyChart(values);
    }

    @Override
    public List<MonthlyChartResponse> getFeedChart(UUID siteId) {
        Map<Integer, BigDecimal> values =
                new java.util.HashMap<>();

        List<FeedEntry> entries =
                feedEntryRepository.findAllBySiteForChart(siteId);

        for (FeedEntry entry : entries) {

            if (entry.getFeedDate() == null
                    || entry.getFeedQuantityKg() == null) {
                continue;
            }

            int month =
                    entry.getFeedDate()
                            .getMonthValue();

            values.merge(
                    month,
                    entry.getFeedQuantityKg(),
                    BigDecimal::add);
        }

        return buildMonthlyChart(values);
    }

    @Override
    public List<MonthlyChartResponse> getHarvestChart(UUID siteId) {
        Map<Integer, BigDecimal> values =
                new java.util.HashMap<>();

        List<Harvest> harvests =
                harvestRepository.findAllBySiteForChart(siteId);

        for (Harvest harvest : harvests) {

            if (harvest.getHarvestDate() == null
                    || harvest.getHarvestQuantityKg() == null) {
                continue;
            }

            int month =
                    harvest.getHarvestDate()
                            .getMonthValue();

            values.merge(
                    month,
                    harvest.getHarvestQuantityKg(),
                    BigDecimal::add);
        }

        return buildMonthlyChart(values);
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

        Site site =
                siteRepository.findById(siteId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(SITE_NOT_FOUND));

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

        RevenueReportResponse revenue =
                getRevenueReport(filter);

        FeedReportResponse feed =
                getFeedReport(filter);

        MedicineReportResponse medicine =
                getMedicineReport(filter);

        ChartsResponse charts =
                ChartsResponse.builder()
                        .revenue(
                                getRevenueChart(siteId))
                        .feed(
                                getFeedChart(siteId))
                        .harvest(
                                getHarvestChart(siteId))
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
    public MedicineReportResponse getMedicineReport(
            ReportFilterRequest request) {

        log.info(
                "Generating medicine report for site {}",
                request.getSiteId());

        Site site = siteRepository.findById(
                        request.getSiteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(SITE_NOT_FOUND));

        Pond pond = null;

        if (request.getPondId() != null) {

            pond = pondRepository.findById(
                            request.getPondId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(POND_NOT_FOUND));
        }

        LocalDateTime fromDateTime =
                request.getFromDate().atStartOfDay();

        LocalDateTime toDateTime =
                request.getToDate().atTime(23, 59, 59);

        List<MedicineEntry> entries =
                medicineRepository.findMedicineReport(
                        request.getSiteId(),
                        request.getPondId(),
                        fromDateTime,
                        toDateTime);

        int count = entries.size();

        BigDecimal totalQuantity =
                entries.stream()
                        .map(MedicineEntry::getQuantity)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        List<MedicineReportItemResponse> details =
                entries.stream()
                        .map(entry -> {

                            List<MedicinePhotoReportResponse> photos =
                                    medicinePhotoRepository
                                            .findByMedicineEntryIdOrderByUploadedAt(
                                                    entry.getId())
                                            .stream()
                                            .map(photo ->
                                                    MedicinePhotoReportResponse.builder()
                                                            .fileName(photo.getFileName())
                                                            .filePath(photo.getFilePath())
                                                            .contentType(photo.getContentType())
                                                            .fileSize(photo.getFileSize())
                                                            .build())
                                            .toList();

                            return MedicineReportItemResponse.builder()
                                    .createdAt(entry.getCreatedAt())
                                    .cycleNumber(entry.getPondCycle().getCycleNumber())
                                    .quantity(entry.getQuantity())
                                    .unit(entry.getUnit().name())
                                    .remarks(entry.getRemarks())
                                    .createdBy(entry.getCreatedBy().getEmployeeCode())
                                    .photos(photos)
                                    .build();
                        })
                        .toList();

        return MedicineReportResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .pondId(
                        pond != null
                                ? pond.getId()
                                : null)

                .pondCode(
                        pond != null
                                ? pond.getPondCode()
                                : null)

                .pondName(
                        pond != null
                                ? pond.getPondName()
                                : null)

                .fromDate(request.getFromDate())
                .toDate(request.getToDate())

                .medicineEntryCount(count)

                .totalMedicineQuantity(totalQuantity)

                .details(details)

                .build();
    }
}