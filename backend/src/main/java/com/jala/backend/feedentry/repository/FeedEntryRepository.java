package com.jala.backend.feedentry.repository;

import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FeedEntryRepository
        extends JpaRepository<FeedEntry, UUID> {

    boolean existsByPondCycleIdAndFeedScheduleIdAndFeedDate(
            UUID pondCycleId,
            UUID feedScheduleId,
            LocalDate feedDate);

    List<FeedEntry> findByPondCycleIdAndFeedDateAndStatus(
            UUID pondCycleId,
            LocalDate feedDate,
            FeedEntryStatus status);

    List<FeedEntry> findByPondCycleIdOrderByFeedDateDesc(
            UUID pondCycleId);
}