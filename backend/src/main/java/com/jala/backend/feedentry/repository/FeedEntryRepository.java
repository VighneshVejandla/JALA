package com.jala.backend.feedentry.repository;

import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

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

    @Query("""
        SELECT COALESCE(SUM(f.feedQuantityKg),0)
        FROM FeedEntry f
        WHERE f.pondCycle.id = :pondCycleId
        AND f.feedDate = :feedDate
        AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
        """)
    BigDecimal getTodayFeedKg(
            UUID pondCycleId,
            LocalDate feedDate);


    @Query("""
        SELECT COALESCE(SUM(f.feedQuantityKg),0)
        FROM FeedEntry f
        WHERE f.pondCycle.id = :pondCycleId
        AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
        """)
    BigDecimal getTotalFeedKg(
            UUID pondCycleId);


    Integer countByPondCycleIdAndFeedDateAndStatus(
            UUID pondCycleId,
            LocalDate feedDate,
            FeedEntryStatus status);
}