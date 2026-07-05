package com.jala.backend.feedschedule.service;

import com.jala.backend.feedschedule.dto.request.CreateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.request.UpdateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.response.FeedScheduleResponse;
import com.jala.backend.feedschedule.mapper.FeedScheduleMapper;
import com.jala.backend.feedschedule.repository.FeedScheduleRepository;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedschedule.entity.FeedSchedule;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedScheduleServiceImpl
        implements FeedScheduleService {

    private final FeedScheduleRepository repository;

    private final PondCycleRepository pondCycleRepository;

    private final FeedScheduleMapper mapper;

    @Override
    @Transactional
    public List<FeedScheduleResponse> createSchedules(
            CreateFeedScheduleRequest request) {

        log.info("Creating feed schedule for pond cycle {}",
                request.getPondCycleId());

        PondCycle cycle = pondCycleRepository
                .findById(request.getPondCycleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond cycle not found"));

        if (cycle.getStatus() != PondCycleStatus.ACTIVE) {

            throw new BadRequestException(
                    "Feed schedules can only be created for active cycles.");
        }

        List<LocalTime> feedingTimes =
                request.getFeedingTimes()
                        .stream()
                        .sorted()
                        .toList();

        Set<LocalTime> uniqueTimes = new HashSet<>(feedingTimes);

        if (uniqueTimes.size() != feedingTimes.size()) {

            throw new BadRequestException(
                    "Duplicate feeding times are not allowed.");
        }

        List<FeedSchedule> schedules = new ArrayList<>();

        int session = 1;

        for (LocalTime feedingTime : feedingTimes) {

            if (repository.existsByPondCycleIdAndFeedingTime(
                    cycle.getId(),
                    feedingTime)) {

                throw new BadRequestException(
                        "Feeding time already exists.");
            }

            FeedSchedule schedule = FeedSchedule.builder()
                    .pondCycle(cycle)
                    .sessionNumber(session++)
                    .feedingTime(feedingTime)
                    .isActive(true)
                    .build();

            schedules.add(schedule);
        }

        repository.saveAll(schedules);

        log.info("Created {} feed sessions",
                schedules.size());

        return schedules.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedScheduleResponse> getSchedulesByCycle(
            UUID pondCycleId) {

        return repository.findByPondCycleIdOrderBySessionNumber(
                        pondCycleId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FeedScheduleResponse updateSchedule(
            UUID id,
            UpdateFeedScheduleRequest request) {

        FeedSchedule schedule = getScheduleOrThrow(id);

        PondCycle cycle = schedule.getPondCycle();

        if (cycle.getStatus() != PondCycleStatus.ACTIVE) {

            throw new BadRequestException(
                    "Cannot update feed schedule for a harvested cycle.");
        }

        if (request.getFeedingTime() != null) {

            if (!request.getFeedingTime()
                    .equals(schedule.getFeedingTime())
                    && repository.existsByPondCycleIdAndFeedingTime(
                    cycle.getId(),
                    request.getFeedingTime())) {

                throw new BadRequestException(
                        "Feeding time already exists.");
            }

            schedule.setFeedingTime(request.getFeedingTime());
        }

        if (request.getIsActive() != null) {
            schedule.setIsActive(request.getIsActive());
        }

        FeedSchedule updated = repository.save(schedule);

        log.info("Feed schedule updated. Session {}",
                updated.getSessionNumber());

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deactivateSchedule(UUID id) {

        FeedSchedule schedule = getScheduleOrThrow(id);

        schedule.setIsActive(false);

        repository.save(schedule);

        log.info("Feed session {} deactivated",
                schedule.getSessionNumber());
    }

    @Override
    @Transactional
    public void activateSchedule(UUID id) {

        FeedSchedule schedule = getScheduleOrThrow(id);

        schedule.setIsActive(true);

        repository.save(schedule);

        log.info("Feed session {} activated",
                schedule.getSessionNumber());
    }

    private FeedSchedule getScheduleOrThrow(UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed schedule not found"));
    }
}
