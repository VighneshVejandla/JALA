package com.jala.backend.analytics.service;

import com.jala.backend.analytics.dto.response.*;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.feeddelivery.repository.SiteDeliveryRepository;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.enums.HarvestStatus;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl
        implements AnalyticsService {

    private final PondRepository pondRepository;

    private final PondCycleRepository pondCycleRepository;

    private final FeedEntryRepository feedEntryRepository;

    private final SiteRepository siteRepository;

    private final FeedInventoryRepository feedInventoryRepository;

    private final SiteDeliveryRepository siteDeliveryRepository;

    private final HarvestRepository harvestRepository;

    private static final String SITE_NOT_FOUND = "Site not found.";

    @Override
    @Transactional(readOnly = true)
    public FeedAnalyticsResponse getPondFeedAnalytics(
            UUID pondId) {

        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond not found."));

        PondCycle activeCycle =
                pondCycleRepository
                        .findByPondIdAndStatus(
                                pondId,
                                PondCycleStatus.ACTIVE)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No active pond cycle found."));

        LocalDate today = DateTimeUtil.today();

        LocalDate weekStart =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY));

        LocalDate monthStart =
                today.withDayOfMonth(1);

        BigDecimal todayFeed =
                feedEntryRepository.getFeedForDate(
                        activeCycle.getId(),
                        today);

        BigDecimal weekFeed =
                feedEntryRepository.getFeedBetweenDates(
                        activeCycle.getId(),
                        weekStart,
                        today);

        BigDecimal monthFeed =
                feedEntryRepository.getFeedBetweenDates(
                        activeCycle.getId(),
                        monthStart,
                        today);

        Integer todayEntries =
                feedEntryRepository.countEntriesForDate(
                        activeCycle.getId(),
                        today);

        Integer weekEntries =
                feedEntryRepository.countEntriesBetweenDates(
                        activeCycle.getId(),
                        weekStart,
                        today);

        Integer monthEntries =
                feedEntryRepository.countEntriesBetweenDates(
                        activeCycle.getId(),
                        monthStart,
                        today);

        return FeedAnalyticsResponse.builder()
                .pondId(pond.getId())
                .pondCode(pond.getPondCode())
                .pondName(pond.getPondName())

                .todayFeedKg(todayFeed)
                .weekFeedKg(weekFeed)
                .monthFeedKg(monthFeed)

                .todayFeedEntries(todayEntries)
                .weekFeedEntries(weekEntries)
                .monthFeedEntries(monthEntries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse getAnalyticsDashboard(
            UUID siteId) {

        Site site =
                siteRepository.findById(siteId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(SITE_NOT_FOUND));

        SiteFeedAnalyticsResponse feed =
                getSiteFeedAnalytics(siteId);

        InventoryAnalyticsResponse inventory =
                getInventoryAnalytics(siteId);

        SiteHarvestAnalyticsResponse harvest =
                getSiteHarvestAnalytics(siteId);

        return AnalyticsDashboardResponse.builder()

                .siteId(site.getId())

                .siteCode(site.getSiteCode())

                .siteName(site.getSiteName())

                .feed(feed)

                .inventory(inventory)

                .harvest(harvest)

                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PondHarvestAnalyticsResponse getPondHarvestAnalytics(
            UUID pondId) {

        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond not found."));

        Integer harvestCount =
                harvestRepository.getHarvestCountByPond(
                        pondId);

        BigDecimal totalHarvest =
                harvestRepository.getTotalHarvestKg(
                        pondId);

        BigDecimal averageHarvest =
                harvestRepository.getAverageHarvestKg(
                        pondId);

        BigDecimal totalRevenue =
                harvestRepository.getTotalRevenue(
                        pondId);

        Harvest latestHarvest =
                harvestRepository
                        .findFirstByPondCyclePondIdAndStatusOrderByHarvestDateDescUploadedAtDesc(
                                pondId,
                                HarvestStatus.ACTIVE)
                        .orElse(null);

        return PondHarvestAnalyticsResponse.builder()

                .pondId(pond.getId())
                .pondCode(pond.getPondCode())
                .pondName(pond.getPondName())

                .harvestCount(harvestCount)

                .totalHarvestKg(totalHarvest)

                .averageHarvestKg(averageHarvest)

                .totalRevenue(totalRevenue)

                .lastHarvestDate(
                        latestHarvest == null
                                ? null
                                : latestHarvest.getHarvestDate())

                .lastHarvestQuantityKg(
                        latestHarvest == null
                                ? null
                                : latestHarvest.getHarvestQuantityKg())

                .lastHarvestRevenue(
                        latestHarvest == null
                                ? null
                                : latestHarvest.getTotalAmount())

                .lastBuyer(
                        latestHarvest == null
                                ? null
                                : latestHarvest.getBuyerName())

                .build();
    }

    @Override
    public SiteHarvestAnalyticsResponse getSiteHarvestAnalytics(
            UUID siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(SITE_NOT_FOUND));

        LocalDate today = DateTimeUtil.today();

        LocalDate weekStart =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY));

        LocalDate monthStart =
                today.withDayOfMonth(1);

        Integer harvestCount =
                harvestRepository.getHarvestCountBySite(
                        siteId);

        BigDecimal todayHarvest =
                harvestRepository.getSiteHarvestBetweenDates(
                        siteId,
                        today,
                        today);

        BigDecimal weekHarvest =
                harvestRepository.getSiteHarvestBetweenDates(
                        siteId,
                        weekStart,
                        today);

        BigDecimal monthHarvest =
                harvestRepository.getSiteHarvestBetweenDates(
                        siteId,
                        monthStart,
                        today);

        BigDecimal todayRevenue =
                harvestRepository.getSiteRevenueBetweenDates(
                        siteId,
                        today,
                        today);

        BigDecimal weekRevenue =
                harvestRepository.getSiteRevenueBetweenDates(
                        siteId,
                        weekStart,
                        today);

        BigDecimal monthRevenue =
                harvestRepository.getSiteRevenueBetweenDates(
                        siteId,
                        monthStart,
                        today);

        return SiteHarvestAnalyticsResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .harvestCount(harvestCount)

                .todayHarvestKg(todayHarvest)
                .weekHarvestKg(weekHarvest)
                .monthHarvestKg(monthHarvest)

                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)

                .build();
    }

    @Override
    public InventoryAnalyticsResponse getInventoryAnalytics(
            UUID siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(SITE_NOT_FOUND));

        FeedInventory inventory =
                feedInventoryRepository.findBySiteId(siteId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found."));

        LocalDate today = DateTimeUtil.today();

        LocalDate weekStart =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY));

        LocalDate monthStart =
                today.withDayOfMonth(1);

        BigDecimal deliveredToday =
                siteDeliveryRepository.getDeliveredForDate(
                        siteId,
                        today);

        BigDecimal deliveredWeek =
                siteDeliveryRepository.getDeliveredBetweenDates(
                        siteId,
                        weekStart,
                        today);

        BigDecimal deliveredMonth =
                siteDeliveryRepository.getDeliveredBetweenDates(
                        siteId,
                        monthStart,
                        today);

        BigDecimal totalDelivered =
                siteDeliveryRepository.getTotalDelivered(
                        siteId);

        BigDecimal consumedToday =
                feedEntryRepository.getConsumedForDate(
                        siteId,
                        today);

        BigDecimal consumedWeek =
                feedEntryRepository.getConsumedBetweenDates(
                        siteId,
                        weekStart,
                        today);

        BigDecimal consumedMonth =
                feedEntryRepository.getConsumedBetweenDates(
                        siteId,
                        monthStart,
                        today);

        BigDecimal totalConsumed =
                feedEntryRepository.getTotalConsumed(
                        siteId);

        int bags =
                inventory.getAvailableKg()
                        .divide(new BigDecimal("25"))
                        .intValue();

        return InventoryAnalyticsResponse.builder()

                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .deliveredTodayKg(deliveredToday)
                .deliveredWeekKg(deliveredWeek)
                .deliveredMonthKg(deliveredMonth)
                .totalDeliveredKg(totalDelivered)

                .consumedTodayKg(consumedToday)
                .consumedWeekKg(consumedWeek)
                .consumedMonthKg(consumedMonth)
                .totalConsumedKg(totalConsumed)

                .availableKg(inventory.getAvailableKg())
                .availableBags(bags)

                .build();
    }

    @Override
    public SiteFeedAnalyticsResponse getSiteFeedAnalytics(
            UUID siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(SITE_NOT_FOUND));

        LocalDate today = DateTimeUtil.today();

        LocalDate weekStart =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY));

        LocalDate monthStart =
                today.withDayOfMonth(1);

        BigDecimal todayFeed =
                feedEntryRepository.getSiteFeedForDate(
                        siteId,
                        today);

        BigDecimal weekFeed =
                feedEntryRepository.getSiteFeedBetweenDates(
                        siteId,
                        weekStart,
                        today);

        BigDecimal monthFeed =
                feedEntryRepository.getSiteFeedBetweenDates(
                        siteId,
                        monthStart,
                        today);

        Integer todayEntries =
                feedEntryRepository.countSiteEntriesForDate(
                        siteId,
                        today);

        Integer weekEntries =
                feedEntryRepository.countSiteEntriesBetweenDates(
                        siteId,
                        weekStart,
                        today);

        Integer monthEntries =
                feedEntryRepository.countSiteEntriesBetweenDates(
                        siteId,
                        monthStart,
                        today);

        Integer pondsToday =
                feedEntryRepository.countPondsFedForDate(
                        siteId,
                        today);

        Integer pondsWeek =
                feedEntryRepository.countPondsFedBetweenDates(
                        siteId,
                        weekStart,
                        today);

        Integer pondsMonth =
                feedEntryRepository.countPondsFedBetweenDates(
                        siteId,
                        monthStart,
                        today);

        return SiteFeedAnalyticsResponse.builder()
                .siteId(site.getId())
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())

                .todayFeedKg(todayFeed)
                .weekFeedKg(weekFeed)
                .monthFeedKg(monthFeed)

                .todayFeedEntries(todayEntries)
                .weekFeedEntries(weekEntries)
                .monthFeedEntries(monthEntries)

                .pondsFedToday(pondsToday)
                .pondsFedWeek(pondsWeek)
                .pondsFedMonth(pondsMonth)

                .build();
    }
}