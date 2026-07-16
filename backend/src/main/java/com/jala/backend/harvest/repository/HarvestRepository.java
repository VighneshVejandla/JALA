package com.jala.backend.harvest.repository;

import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.enums.HarvestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HarvestRepository
        extends JpaRepository<Harvest, UUID> {

    @EntityGraph(attributePaths = {"pondCycle", "uploadedBy"})
    List<Harvest> findByPondCycleIdOrderByHarvestDateDesc(
            UUID pondCycleId,
            Pageable pageable);

    List<Harvest> findByPondCycleIdAndStatusOrderByHarvestDateDesc(
            UUID pondCycleId,
            HarvestStatus status);

    long countByPondCycleId(
            UUID pondCycleId);

    long countByPondCycleIdAndStatus(
            UUID pondCycleId,
            HarvestStatus status);

    Optional<Harvest> findByIdAndStatus(
            UUID id,
            HarvestStatus status);

    Optional<Harvest> findByPondCycleIdAndStatus(
            UUID pondCycleId,
            HarvestStatus status);

    @Query("""
            SELECT COUNT(h)
            FROM Harvest h
            WHERE h.pondCycle.pond.id = :pondId
            AND h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    Integer getHarvestCount(
            UUID pondId);

    Optional<Harvest>
    findFirstByPondCyclePondIdAndStatusOrderByHarvestDateDescUploadedAtDesc(
            UUID pondId,
            HarvestStatus status);

    @EntityGraph(attributePaths = {"pondCycle", "uploadedBy", "cancelledBy"})
    List<Harvest> findByPondCyclePondIdOrderByHarvestDateDescUploadedAtDesc(
            UUID pondId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"pondCycle"})
    List<Harvest> findByPondCyclePondId(
            UUID pondId);

    /** (cycleId, active harvest count) per cycle of the pond in one query. */
    @Query("""
            SELECT h.pondCycle.id, COUNT(h)
            FROM Harvest h
            WHERE h.pondCycle.pond.id = :pondId
            AND h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            GROUP BY h.pondCycle.id
            """)
    List<Object[]> countActiveByCycleForPond(
            UUID pondId);

    @Query("""
        SELECT COALESCE(SUM(h.harvestQuantityKg),0)
        FROM Harvest h
        WHERE h.pondCycle.pond.site.id = :siteId
        AND h.harvestDate = :date
        AND h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
        """)
    BigDecimal getTodayHarvestKg(
            UUID siteId,
            LocalDate date);

    @Query("""
        SELECT COALESCE(SUM(h.totalAmount),0)
        FROM Harvest h
        WHERE h.pondCycle.pond.site.id = :siteId
        AND h.harvestDate = :date
        AND h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
        """)
    BigDecimal getTodayRevenue(
            UUID siteId,
            LocalDate date);

    @Query("""
        SELECT h
        FROM Harvest h
        JOIN FETCH h.pondCycle pc
        JOIN FETCH pc.pond p
        JOIN FETCH p.site
        WHERE h.status =
              com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
        AND (
            LOWER(COALESCE(h.buyerName,'')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(h.remarks,'')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY h.harvestDate DESC
        """)
    List<Harvest> search(
            String keyword,
            Pageable pageable);

    /** (month, revenue total) rows — aggregation happens in SQL. */
    @Query("""
            SELECT MONTH(h.harvestDate), COALESCE(SUM(h.totalAmount), 0)
            FROM Harvest h
            WHERE h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            AND h.pondCycle.pond.site.id = :siteId
            GROUP BY MONTH(h.harvestDate)
            """)
    List<Object[]> sumRevenueByMonth(
            UUID siteId);

    /** (month, harvested kg total) rows — aggregation happens in SQL. */
    @Query("""
            SELECT MONTH(h.harvestDate), COALESCE(SUM(h.harvestQuantityKg), 0)
            FROM Harvest h
            WHERE h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            AND h.pondCycle.pond.site.id = :siteId
            GROUP BY MONTH(h.harvestDate)
            """)
    List<Object[]> sumHarvestKgByMonth(
            UUID siteId);

    @Query("""
            SELECT h
            FROM Harvest h
            WHERE h.status = com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            AND h.harvestDate BETWEEN :fromDate AND :toDate
            AND h.pondCycle.pond.site.id = :siteId
            AND (:pondId IS NULL OR h.pondCycle.pond.id = :pondId)
            ORDER BY h.harvestDate DESC
            """)
    List<Harvest> findRevenueReport(
            UUID siteId,
            UUID pondId,
            LocalDate fromDate,
            LocalDate toDate);

    @Query("""
            SELECT COUNT(h)
            FROM Harvest h
            WHERE h.pondCycle.pond.id=:pondId
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    Integer getHarvestCountByPond(
            UUID pondId);

    @Query("""
            SELECT COALESCE(SUM(h.harvestQuantityKg),0)
            FROM Harvest h
            WHERE h.pondCycle.pond.id=:pondId
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    BigDecimal getTotalHarvestKg(
            UUID pondId);

    @Query("""
            SELECT COALESCE(AVG(h.harvestQuantityKg),0)
            FROM Harvest h
            WHERE h.pondCycle.pond.id=:pondId
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    BigDecimal getAverageHarvestKg(
            UUID pondId);

    @Query("""
            SELECT COALESCE(SUM(h.totalAmount),0)
            FROM Harvest h
            WHERE h.pondCycle.pond.id=:pondId
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    BigDecimal getTotalRevenue(
            UUID pondId);

    @Query("""
            SELECT COUNT(h)
            FROM Harvest h
            WHERE h.pondCycle.pond.site.id=:siteId
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    Integer getHarvestCountBySite(
            UUID siteId);

    @Query("""
            SELECT COALESCE(SUM(h.harvestQuantityKg),0)
            FROM Harvest h
            WHERE h.pondCycle.pond.site.id=:siteId
            AND h.harvestDate BETWEEN :startDate AND :endDate
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    BigDecimal getSiteHarvestBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(h.totalAmount),0)
            FROM Harvest h
            WHERE h.pondCycle.pond.site.id=:siteId
            AND h.harvestDate BETWEEN :startDate AND :endDate
            AND h.status=com.jala.backend.harvest.enums.HarvestStatus.ACTIVE
            """)
    BigDecimal getSiteRevenueBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

}