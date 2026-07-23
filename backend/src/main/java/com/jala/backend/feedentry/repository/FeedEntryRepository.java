package com.jala.backend.feedentry.repository;

import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.id = :pondCycleId
            AND f.feedDate = :date
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getFeedForDate(
            UUID pondCycleId,
            LocalDate date);


    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.id = :pondCycleId
            AND f.feedDate BETWEEN :startDate AND :endDate
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getFeedBetweenDates(
            UUID pondCycleId,
            LocalDate startDate,
            LocalDate endDate);


    @Query("""
            SELECT COUNT(f)
            FROM FeedEntry f
            WHERE f.pondCycle.id = :pondCycleId
            AND f.feedDate = :date
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    Integer countEntriesForDate(
            UUID pondCycleId,
            LocalDate date);


    @Query("""
            SELECT COUNT(f)
            FROM FeedEntry f
            WHERE f.pondCycle.id = :pondCycleId
            AND f.feedDate BETWEEN :startDate AND :endDate
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    Integer countEntriesBetweenDates(
            UUID pondCycleId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id = :siteId
            AND f.feedDate = :date
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getSiteFeedForDate(
            UUID siteId,
            LocalDate date);

    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id = :siteId
            AND f.feedDate BETWEEN :startDate AND :endDate
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getSiteFeedBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
            SELECT COUNT(f)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id = :siteId
            AND f.feedDate = :date
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    Integer countSiteEntriesForDate(
            UUID siteId,
            LocalDate date);

    @Query("""
            SELECT COUNT(f)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id = :siteId
            AND f.feedDate BETWEEN :startDate AND :endDate
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    Integer countSiteEntriesBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
            SELECT COUNT(DISTINCT f.pondCycle.pond.id)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id = :siteId
            AND f.feedDate = :date
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    Integer countPondsFedForDate(
            UUID siteId,
            LocalDate date);

    @Query("""
            SELECT COUNT(DISTINCT f.pondCycle.pond.id)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id = :siteId
            AND f.feedDate BETWEEN :startDate AND :endDate
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    Integer countPondsFedBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id=:siteId
            AND f.feedDate=:date
            AND f.status=com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getConsumedForDate(
            UUID siteId,
            LocalDate date);

    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id=:siteId
            AND f.feedDate BETWEEN :startDate AND :endDate
            AND f.status=com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getConsumedBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(f.feedQuantityKg),0)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.site.id=:siteId
            AND f.status=com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            """)
    BigDecimal getTotalConsumed(
            UUID siteId);

    @Query("""
            SELECT f
            FROM FeedEntry f
            JOIN FETCH f.pondCycle
            JOIN FETCH f.feedSchedule
            JOIN FETCH f.createdBy
            WHERE f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            AND f.feedDate BETWEEN :fromDate AND :toDate
            AND f.pondCycle.pond.site.id = :siteId
            AND (:pondId IS NULL OR f.pondCycle.pond.id = :pondId)
            ORDER BY f.feedDate DESC
            """)
    List<FeedEntry> findFeedReport(
            UUID siteId,
            UUID pondId,
            LocalDate fromDate,
            LocalDate toDate);

    /** (month, fed kg total) rows — aggregation happens in SQL. */
    @Query("""
            SELECT MONTH(f.feedDate), COALESCE(SUM(f.feedQuantityKg), 0)
            FROM FeedEntry f
            WHERE f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            AND f.pondCycle.pond.site.id = :siteId
            GROUP BY MONTH(f.feedDate)
            """)
    List<Object[]> sumFeedKgByMonth(
            UUID siteId);

    /** (feedDate, fed kg total) rows from a start date, one row per day. */
    @Query("""
            SELECT f.feedDate, COALESCE(SUM(f.feedQuantityKg), 0)
            FROM FeedEntry f
            WHERE f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            AND f.pondCycle.pond.site.id = :siteId
            AND f.feedDate >= :fromDate
            GROUP BY f.feedDate
            ORDER BY f.feedDate
            """)
    List<Object[]> sumFeedKgByDay(
            UUID siteId,
            java.time.LocalDate fromDate);

    long countByPondCycleIdAndStatus(
            UUID pondCycleId,
            FeedEntryStatus status);

    /** (cycleId, active entry count) per cycle of the pond in one query. */
    @Query("""
            SELECT f.pondCycle.id, COUNT(f)
            FROM FeedEntry f
            WHERE f.pondCycle.pond.id = :pondId
            AND f.status = com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
            GROUP BY f.pondCycle.id
            """)
    List<Object[]> countActiveByCycleForPond(
            UUID pondId);

    @EntityGraph(attributePaths = {"feedSchedule", "createdBy", "cancelledBy"})
    List<FeedEntry> findByPondCyclePondIdOrderByFeedDateDescIdDesc(
            UUID pondId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"pondCycle", "feedSchedule"})
    List<FeedEntry> findByPondCyclePondId(
            UUID pondId);

    @Query("""
        SELECT f
        FROM FeedEntry f
        JOIN FETCH f.pondCycle pc
        JOIN FETCH pc.pond p
        JOIN FETCH p.site
        WHERE f.status =
              com.jala.backend.feedentry.enums.FeedEntryStatus.ACTIVE
        AND (
            LOWER(COALESCE(f.remarks,'')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY f.feedDate DESC
        """)
    List<FeedEntry> search(
            String keyword,
            Pageable pageable);

}