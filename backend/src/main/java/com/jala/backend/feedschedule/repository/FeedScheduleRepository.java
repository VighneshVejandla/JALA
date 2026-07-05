package com.jala.backend.feedschedule.repository;

import com.jala.backend.feedschedule.entity.FeedSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface FeedScheduleRepository
        extends JpaRepository<FeedSchedule, UUID> {

    List<FeedSchedule> findByPondCycleIdOrderBySessionNumber(
            UUID pondCycleId);

    boolean existsByPondCycleIdAndFeedingTime(
            UUID pondCycleId,
            LocalTime feedingTime);

    long countByPondCycleId(UUID pondCycleId);
}