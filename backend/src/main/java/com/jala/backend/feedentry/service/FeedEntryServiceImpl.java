package com.jala.backend.feedentry.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.feedentry.dto.request.CreateFeedEntryRequest;
import com.jala.backend.feedentry.dto.request.UpdateFeedEntryRequest;
import com.jala.backend.feedentry.dto.response.FeedEntryResponse;
import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import com.jala.backend.feedentry.mapper.FeedEntryMapper;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.feedschedule.entity.FeedSchedule;
import com.jala.backend.feedschedule.repository.FeedScheduleRepository;
import com.jala.backend.notification.service.NotificationService;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.siteaccess.service.SiteAccessService;
import com.jala.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedEntryServiceImpl
        implements FeedEntryService {

    private final FeedEntryRepository repository;

    private final PondCycleRepository pondCycleRepository;

    private final FeedScheduleRepository feedScheduleRepository;

    private final FeedEntryMapper mapper;

    private final CurrentUserService currentUserService;

    private final SiteAccessService siteAccessService;

    private final NotificationService notificationService;

    @Override
    @Transactional
    public FeedEntryResponse createFeedEntry(
            CreateFeedEntryRequest request) {

        log.info("Creating feed entry");

        siteAccessService.checkPondCycleAccess(request.getPondCycleId());

        PondCycle cycle = pondCycleRepository
                .findById(request.getPondCycleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond cycle not found"));

        if (cycle.getStatus() != PondCycleStatus.ACTIVE) {

            throw new BadRequestException(
                    "Feed can only be entered for active cycles.");
        }

        FeedSchedule schedule = feedScheduleRepository
                .findById(request.getFeedScheduleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed schedule not found"));

        if (!schedule.getPondCycle().getId().equals(cycle.getId())) {

            throw new BadRequestException(
                    "Feed schedule does not belong to this pond cycle.");
        }

        if (repository.existsByPondCycleIdAndFeedScheduleIdAndFeedDate(
                cycle.getId(),
                schedule.getId(),
                request.getFeedDate())) {

            throw new BadRequestException(
                    "Feed already entered for this session today.");
        }

        User worker = currentUserService.getCurrentUser();

        FeedEntry entry = mapper.toEntity(request);

        entry.setPondCycle(cycle);

        entry.setFeedSchedule(schedule);

        entry.setCreatedBy(worker);

        FeedEntry saved = repository.save(entry);

        notificationService.createFeedNotification(
                cycle.getPond().getSite().getId(),
                cycle.getPond().getId(),
                cycle.getPond().getSite().getSiteCode(),
                cycle.getPond().getPondCode(),
                schedule.getSessionNumber(),
                request.getFeedQuantityKg().toPlainString());

        log.info("Feed entry created successfully");

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedEntryResponse> getFeedEntries(
            UUID pondCycleId,
            LocalDate date) {

        siteAccessService.checkPondCycleAccess(pondCycleId);

        return repository
                .findByPondCycleIdAndFeedDateAndStatus(
                        pondCycleId,
                        date,
                        FeedEntryStatus.ACTIVE)
                .stream()
                .sorted(Comparator.comparing(
                        e -> e.getFeedSchedule().getSessionNumber()))
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FeedEntryResponse updateFeedEntry(
            UUID id,
            UpdateFeedEntryRequest request) {

        FeedEntry entry = getFeedEntryOrThrow(id);

        siteAccessService.checkPondCycleAccess(
                entry.getPondCycle().getId());

        if (entry.getPondCycle().getStatus()
                != PondCycleStatus.ACTIVE) {

            throw new BadRequestException(
                    "Cannot update feed for a harvested cycle.");
        }

        if (request.getFeedSize() != null) {
            entry.setFeedSize(request.getFeedSize());
        }

        if (request.getFeedQuantityKg() != null) {
            entry.setFeedQuantityKg(request.getFeedQuantityKg());
        }

        if (request.getRemarks() != null) {
            entry.setRemarks(request.getRemarks());
        }

        FeedEntry updated = repository.save(entry);

        log.info("Feed entry updated {}", updated.getId());

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void cancelFeedEntry(
            UUID id,
            String reason) {

        FeedEntry entry = getFeedEntryOrThrow(id);

        siteAccessService.checkPondCycleAccess(
                entry.getPondCycle().getId());

        if (entry.getStatus() ==
                FeedEntryStatus.CANCELLED) {

            throw new BadRequestException(
                    "Feed entry already cancelled.");
        }

        User user = currentUserService.getCurrentUser();

        entry.setStatus(
                FeedEntryStatus.CANCELLED);

        entry.setCancelledBy(user);

        entry.setCancelledAt(DateTimeUtil.now());

        entry.setCancellationReason(reason);

        repository.save(entry);

        log.info("Feed entry cancelled {}", id);
    }

    @Override
    @Transactional
    public void restoreFeedEntry(UUID id) {

        FeedEntry entry = getFeedEntryOrThrow(id);

        siteAccessService.checkPondCycleAccess(
                entry.getPondCycle().getId());

        if (entry.getStatus()
                == FeedEntryStatus.ACTIVE) {

            throw new BadRequestException(
                    "Feed entry is already active.");
        }

        entry.setStatus(
                FeedEntryStatus.ACTIVE);

        entry.setCancelledBy(null);

        entry.setCancelledAt(null);

        entry.setCancellationReason(null);

        repository.save(entry);

        log.info("Feed entry restored {}", id);
    }

    private FeedEntry getFeedEntryOrThrow(UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed entry not found"));
    }
}