package com.jala.backend.medicine.repository;

import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

public interface MedicineRepository
        extends JpaRepository<MedicineEntry, UUID> {

    @EntityGraph(attributePaths = {"pondCycle", "createdBy"})
    List<MedicineEntry> findByPondCycleIdAndStatusOrderByCreatedAtDesc(
            UUID pondCycleId,
            MedicineStatus status,
            Pageable pageable);

    @Query("""
            SELECT COUNT(m)
            FROM MedicineEntry m
            WHERE m.pondCycle.id = :pondCycleId
            AND m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            """)
    Integer getMedicineEntryCount(
            UUID pondCycleId);


    @Query("""
            SELECT COALESCE(SUM(m.quantity),0)
            FROM MedicineEntry m
            WHERE m.pondCycle.id = :pondCycleId
            AND m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            """)
    BigDecimal getTotalMedicineQuantity(
            UUID pondCycleId);

    @Query("""
            SELECT m
            FROM MedicineEntry m
            JOIN FETCH m.pondCycle
            JOIN FETCH m.createdBy
            WHERE m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            AND m.createdAt >= :fromDateTime
            AND m.createdAt < :toDateTimeExclusive
            AND m.pondCycle.pond.site.id = :siteId
            AND (:pondId IS NULL
                 OR m.pondCycle.pond.id = :pondId)
            ORDER BY m.createdAt DESC
            """)
    List<MedicineEntry> findMedicineReport(
            UUID siteId,
            UUID pondId,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTimeExclusive);

    @Query("""
            SELECT MAX(m.createdAt)
            FROM MedicineEntry m
            WHERE m.pondCycle.id = :pondCycleId
            AND m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            """)
    LocalDateTime getLastMedicineDate(
            UUID pondCycleId);

    @Query("""
        SELECT m
        FROM MedicineEntry m
        JOIN FETCH m.pondCycle pc
        JOIN FETCH pc.pond p
        JOIN FETCH p.site
        WHERE m.status =
              com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
        AND (
            LOWER(COALESCE(m.remarks,'')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY m.createdAt DESC
        """)
    List<MedicineEntry> search(
            String keyword,
            Pageable pageable);

    long countByPondCycleIdAndStatus(
            UUID pondCycleId,
            MedicineStatus status);

    /** (cycleId, active entry count) per cycle of the pond in one query. */
    @Query("""
            SELECT m.pondCycle.id, COUNT(m)
            FROM MedicineEntry m
            WHERE m.pondCycle.pond.id = :pondId
            AND m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            GROUP BY m.pondCycle.id
            """)
    List<Object[]> countActiveByCycleForPond(
            UUID pondId);

    @EntityGraph(attributePaths = {"pondCycle", "createdBy", "cancelledBy"})
    List<MedicineEntry> findByPondCyclePondIdOrderByCreatedAtDesc(
            UUID pondId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"pondCycle"})
    List<MedicineEntry> findByPondCyclePondId(
            UUID pondId);
}