package com.jala.backend.history.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.PageRequestUtil;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.history.dto.response.*;
import com.jala.backend.history.mapper.HistoryMapper;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final SiteAccessService siteAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<PondCycleHistoryResponse> getPondCycleHistory(
            UUID pondId) {

        log.info(
                "Fetching pond cycle history for pond {}",
                pondId);

        siteAccessService.checkPondAccess(pondId);

        // One aggregate per child table instead of three counts per cycle.
        Map<UUID, Integer> feedCounts =
                toCountMap(feedEntryRepository
                        .countActiveByCycleForPond(pondId));

        Map<UUID, Integer> medicineCounts =
                toCountMap(medicineRepository
                        .countActiveByCycleForPond(pondId));

        Map<UUID, Integer> harvestCounts =
                toCountMap(harvestRepository
                        .countActiveByCycleForPond(pondId));

        return pondCycleRepository
                .findByPondIdOrderByCycleNumberDesc(pondId)
                .stream()
                .map(cycle -> {

                    PondCycleHistoryResponse response =
                            mapper.toResponse(cycle);

                    response.setTotalFeedEntries(
                            feedCounts.getOrDefault(cycle.getId(), 0));

                    response.setTotalMedicineEntries(
                            medicineCounts.getOrDefault(cycle.getId(), 0));

                    response.setTotalHarvests(
                            harvestCounts.getOrDefault(cycle.getId(), 0));

                    response.setCurrentCycle(
                            cycle.getStatus() == PondCycleStatus.ACTIVE);

                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestHistoryResponse> getHarvestHistory(
            UUID pondId,
            Integer page,
            Integer size) {

        log.info(
                "Fetching harvest history for pond {}",
                pondId);

        siteAccessService.checkPondAccess(pondId);

        return harvestRepository
                .findByPondCyclePondIdOrderByHarvestDateDescUploadedAtDesc(
                        pondId,
                        PageRequestUtil.of(page, size))
                .stream()
                .map(mapper::toHarvestResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedHistoryResponse> getFeedHistory(
            UUID pondId,
            Integer page,
            Integer size) {

        log.info(
                "Fetching feed history for pond {}",
                pondId);

        siteAccessService.checkPondAccess(pondId);

        return feedEntryRepository
                .findByPondCyclePondIdOrderByFeedDateDescIdDesc(
                        pondId,
                        PageRequestUtil.of(page, size))
                .stream()
                .map(mapper::toFeedHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineHistoryResponse> getMedicineHistory(
            UUID pondId,
            Integer page,
            Integer size) {

        log.info(
                "Fetching medicine history for pond {}",
                pondId);

        siteAccessService.checkPondAccess(pondId);

        List<MedicineEntry> entries = medicineRepository
                .findByPondCyclePondIdOrderByCreatedAtDesc(
                        pondId,
                        PageRequestUtil.of(page, size));

        Map<UUID, List<MedicinePhoto>> photosByEntry =
                loadPhotosByEntry(entries);

        return entries.stream()
                .map(entry -> {

                    MedicineHistoryResponse response =
                            mapper.toMedicineHistoryResponse(entry);

                    response.setPhotos(
                            photosByEntry
                                    .getOrDefault(entry.getId(), List.of())
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

        siteAccessService.checkPondAccess(pondId);

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
                .findByPondCyclePondId(pondId)
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

    /** One query for all photos of the page instead of one per entry. */
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

    private static Map<UUID, Integer> toCountMap(List<Object[]> rows) {

        Map<UUID, Integer> counts = new HashMap<>();

        for (Object[] row : rows) {
            counts.put(
                    (UUID) row[0],
                    ((Number) row[1]).intValue());
        }

        return counts;
    }
}
