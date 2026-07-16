package com.jala.backend.history.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.enums.HarvestStatus;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.history.dto.response.*;
import com.jala.backend.history.mapper.HistoryMapper;
import com.jala.backend.medicine.enums.MedicineStatus;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryServiceImpl
        implements HistoryService {

    private final PondCycleRepository pondCycleRepository;

    private final FeedEntryRepository feedEntryRepository;

    private final MedicineRepository medicineRepository;

    private final HarvestRepository harvestRepository;

    private final HistoryMapper mapper;

    private final MedicinePhotoRepository medicinePhotoRepository;

    private final PondRepository pondRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PondCycleHistoryResponse> getPondCycleHistory(
            UUID pondId) {

        log.info(
                "Fetching pond cycle history for pond {}",
                pondId);

        return pondCycleRepository
                .findByPondIdOrderByCycleNumberDesc(pondId)
                .stream()
                .map(cycle -> {

                    PondCycleHistoryResponse response =
                            mapper.toResponse(cycle);

                    response.setTotalFeedEntries(
                            (int) feedEntryRepository
                                    .countByPondCycleIdAndStatus(
                                            cycle.getId(),
                                            FeedEntryStatus.ACTIVE));

                    response.setTotalMedicineEntries(
                            (int) medicineRepository
                                    .countByPondCycleIdAndStatus(
                                            cycle.getId(),
                                            MedicineStatus.ACTIVE));

                    response.setTotalHarvests(
                            (int) harvestRepository
                                    .countByPondCycleIdAndStatus(
                                            cycle.getId(),
                                            HarvestStatus.ACTIVE));

                    response.setCurrentCycle(
                            cycle.getStatus() == PondCycleStatus.ACTIVE);

                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestHistoryResponse> getHarvestHistory(
            UUID pondId) {

        log.info(
                "Fetching harvest history for pond {}",
                pondId);

        return harvestRepository
                .findByPondCyclePondIdOrderByHarvestDateDescUploadedAtDesc(
                        pondId)
                .stream()
                .map(mapper::toHarvestResponse)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedHistoryResponse> getFeedHistory(
            UUID pondId) {

        log.info(
                "Fetching feed history for pond {}",
                pondId);

        return feedEntryRepository
                .findByPondCyclePondIdOrderByFeedDateDescIdDesc(
                        pondId)
                .stream()
                .map(mapper::toFeedHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineHistoryResponse> getMedicineHistory(
            UUID pondId) {

        log.info(
                "Fetching medicine history for pond {}",
                pondId);

        return medicineRepository
                .findByPondCyclePondIdOrderByCreatedAtDesc(
                        pondId)
                .stream()
                .map(entry -> {

                    MedicineHistoryResponse response =
                            mapper.toMedicineHistoryResponse(entry);

                    response.setPhotos(

                            medicinePhotoRepository
                                    .findByMedicineEntryIdOrderByUploadedAt(
                                            entry.getId())
                                    .stream()
                                    .map(mapper::toMedicinePhotoResponse)
                                    .toList());

                    return response;

                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PondTimelineResponse getTimeline(
            UUID pondId) {

        log.info("Fetching timeline for pond {}", pondId);

        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond not found."));

        List<PondTimelineItemResponse> timeline =
                new ArrayList<>();

        // -------------------------------------------------
        // Pond Cycles
        // -------------------------------------------------

        pondCycleRepository
                .findByPondIdOrderByCycleNumberDesc(pondId)
                .forEach(cycle -> {

                    if (cycle.getCreatedAt() != null) {

                        timeline.add(
                                PondTimelineItemResponse.builder()
                                        .referenceId(cycle.getId())
                                        .referenceType("CYCLE")
                                        .eventTime(cycle.getCreatedAt())
                                        .eventType("CYCLE")
                                        .title("Cycle Started")
                                        .description(
                                                "Cycle " +
                                                        cycle.getCycleNumber() +
                                                        " started")
                                        .cycleNumber(
                                                cycle.getCycleNumber())
                                        .build());
                    }

                    if (cycle.getStockingDate() != null) {

                        timeline.add(
                                PondTimelineItemResponse.builder()
                                        .referenceId(cycle.getId())
                                        .referenceType("STOCKING")
                                        .eventTime(
                                                cycle.getStockingDate()
                                                        .atStartOfDay())
                                        .eventType("STOCKING")
                                        .title("Stocking Completed")
                                        .description(
                                                cycle.getSpecies()
                                                        + " - "
                                                        + cycle.getShrimpCount()
                                                        + " shrimp")
                                        .cycleNumber(
                                                cycle.getCycleNumber())
                                        .build());
                    }
                });

        // -------------------------------------------------
        // Feed Entries
        // -------------------------------------------------

        feedEntryRepository
                .findByPondCyclePondId(pondId)
                .forEach(feed -> timeline.add(
                        PondTimelineItemResponse.builder()
                                .referenceId(feed.getId())
                                .referenceType("FEED")
                                .eventTime(
                                        feed.getFeedDate()
                                                .atTime(LocalTime.NOON))
                                .eventType("FEED")
                                .title("Feed Entry")
                                .description(
                                        feed.getFeedQuantityKg()
                                                + " KG | Session "
                                                + feed.getFeedSchedule()
                                                .getSessionNumber())
                                .cycleNumber(
                                        feed.getPondCycle()
                                                .getCycleNumber())
                                .build()));

        // -------------------------------------------------
        // Medicine Entries
        // -------------------------------------------------

        medicineRepository
                .findByPondCyclePondIdOrderByCreatedAtDesc(
                        pondId)
                .forEach(medicine -> timeline.add(
                        PondTimelineItemResponse.builder()
                                .referenceId(medicine.getId())
                                .referenceType("MEDICINE")
                                .eventTime(
                                        medicine.getCreatedAt())
                                .eventType("MEDICINE")
                                .title("Medicine Added")
                                .description(
                                        medicine.getQuantity()
                                                + " "
                                                + medicine.getUnit())
                                .cycleNumber(
                                        medicine.getPondCycle()
                                                .getCycleNumber())
                                .build()));

        // -------------------------------------------------
        // Harvests
        // -------------------------------------------------

        harvestRepository
                .findByPondCyclePondId(pondId)
                .forEach(harvest -> timeline.add(
                        PondTimelineItemResponse.builder()
                                .referenceId(harvest.getId())
                                .referenceType("HARVEST")
                                .eventTime(
                                        harvest.getUploadedAt())
                                .eventType("HARVEST")
                                .title("Harvest")
                                .description(
                                        harvest.getHarvestQuantityKg()
                                                + " KG")
                                .cycleNumber(
                                        harvest.getPondCycle()
                                                .getCycleNumber())
                                .build()));

        timeline.sort(
                Comparator.comparing(
                                PondTimelineItemResponse::getEventTime)
                        .reversed());

        return PondTimelineResponse.builder()
                .pondId(pond.getId())
                .pondCode(pond.getPondCode())
                .pondName(pond.getPondName())
                .timeline(timeline)
                .build();
    }
}