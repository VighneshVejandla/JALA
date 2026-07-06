package com.jala.backend.dashboard.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.dashboard.dto.response.PondDashboardResponse;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.enums.MedicineStatus;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.enums.HarvestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PondDashboardServiceImpl
        implements PondDashboardService {

    private final PondRepository pondRepository;

    private final PondCycleRepository pondCycleRepository;

    private final FeedEntryRepository feedEntryRepository;

    private final MedicineRepository medicineRepository;

    private final MedicinePhotoRepository medicinePhotoRepository;

    private final HarvestRepository harvestRepository;

    @Override
    @Transactional(readOnly = true)
    public PondDashboardResponse getDashboard(
            UUID pondId) {

        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond not found."));

        PondCycle activeCycle = pondCycleRepository
                .findByPondIdAndStatus(
                        pondId,
                        PondCycleStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No active cycle found."));

        Long daysSinceStocking = null;

        if (activeCycle.getStockingDate() != null) {

            daysSinceStocking =
                    ChronoUnit.DAYS.between(
                            activeCycle.getStockingDate(),
                            LocalDate.now());
        }

        BigDecimal todayFeedKg =
                feedEntryRepository.getTodayFeedKg(
                        activeCycle.getId(),
                        LocalDate.now());

        BigDecimal totalFeedKg =
                feedEntryRepository.getTotalFeedKg(
                        activeCycle.getId());

        Integer todayFeedEntries =
                feedEntryRepository.countByPondCycleIdAndFeedDateAndStatus(
                        activeCycle.getId(),
                        LocalDate.now(),
                        FeedEntryStatus.ACTIVE);
        Integer medicineEntryCount =
                medicineRepository.getMedicineEntryCount(
                        activeCycle.getId());

        BigDecimal totalMedicineQuantity =
                medicineRepository.getTotalMedicineQuantity(
                        activeCycle.getId());

        LocalDateTime lastMedicineDate =
                medicineRepository.getLastMedicineDate(
                        activeCycle.getId());

        Integer medicinePhotoCount =
                medicineRepository
                        .findByPondCycleIdAndStatusOrderByCreatedAtDesc(
                                activeCycle.getId(),
                                MedicineStatus.ACTIVE)
                        .stream()
                        .mapToInt(entry ->
                                (int) medicinePhotoRepository
                                        .countByMedicineEntryId(
                                                entry.getId()))
                        .sum();

        Integer harvestCount =
                harvestRepository.getHarvestCount(
                        pond.getId());

        if (harvestCount == null) {
            harvestCount = 0;
        }

        Harvest latestHarvest =
                harvestRepository
                        .findFirstByPondCyclePondIdAndStatusOrderByHarvestDateDescUploadedAtDesc(
                                pond.getId(),
                                HarvestStatus.ACTIVE)
                        .orElse(null);

        LocalDate lastHarvestDate = null;

        BigDecimal lastHarvestQuantity = null;

        BigDecimal lastHarvestAmount = null;

        String lastBuyer = null;

        if (latestHarvest != null) {

            lastHarvestDate =
                    latestHarvest.getHarvestDate();

            lastHarvestQuantity =
                    latestHarvest.getHarvestQuantityKg();

            lastHarvestAmount =
                    latestHarvest.getTotalAmount();

            lastBuyer =
                    latestHarvest.getBuyerName();
        }

        return PondDashboardResponse.builder()
                .pondId(pond.getId())
                .pondCode(pond.getPondCode())
                .pondName(pond.getPondName())

                .siteId(pond.getSite().getId())
                .siteCode(pond.getSite().getSiteCode())
                .siteName(pond.getSite().getSiteName())

                .activeCycleId(activeCycle.getId())
                .cycleNumber(activeCycle.getCycleNumber())
                .cycleStatus(activeCycle.getStatus())

                .stockingCompleted(
                        isStockingCompleted(activeCycle))

                .species(activeCycle.getSpecies())
                .stockingDate(activeCycle.getStockingDate())
                .daysSinceStocking(daysSinceStocking)
                .shrimpCount(activeCycle.getShrimpCount())

                // Feed Summary
                .todayFeedKg(todayFeedKg)
                .totalFeedKg(totalFeedKg)
                .todayFeedEntries(todayFeedEntries)

                .medicineEntryCount(medicineEntryCount)
                .totalMedicineQuantity(totalMedicineQuantity)
                .lastMedicineDate(lastMedicineDate)
                .medicinePhotoCount(medicinePhotoCount)

                .harvestCount(harvestCount)

                .lastHarvestDate(lastHarvestDate)

                .lastHarvestQuantityKg(lastHarvestQuantity)

                .lastHarvestAmount(lastHarvestAmount)

                .lastBuyerName(lastBuyer)

                .build();
    }

    private boolean isStockingCompleted(
            PondCycle cycle) {

        return cycle.getSpecies() != null
                && cycle.getStockingDate() != null
                && cycle.getShrimpCount() != null;
    }
}