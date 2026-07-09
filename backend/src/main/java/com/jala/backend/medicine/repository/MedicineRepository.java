package com.jala.backend.medicine.repository;

import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

public interface MedicineRepository
        extends JpaRepository<MedicineEntry, UUID> {

    List<MedicineEntry> findByPondCycleIdAndStatusOrderByCreatedAtDesc(
            UUID pondCycleId,
            MedicineStatus status);

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
            WHERE m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            AND m.createdAt BETWEEN :fromDateTimeStart
            AND :fromDateTimeEnd
            AND m.pondCycle.pond.site.id = :siteId
            AND (:pondId IS NULL
                 OR m.pondCycle.pond.id = :pondId)
            ORDER BY m.createdAt DESC
            """)
    List<MedicineEntry> findMedicineReport(
            UUID siteId,
            UUID pondId,
            LocalDateTime fromDateTimeStart,
            LocalDateTime fromDateTimeEnd);

    @Query("""
            SELECT MAX(m.createdAt)
            FROM MedicineEntry m
            WHERE m.pondCycle.id = :pondCycleId
            AND m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            """)
    LocalDateTime getLastMedicineDate(
            UUID pondCycleId);

    long countByPondCycleIdAndStatus(
            UUID pondCycleId,
            MedicineStatus status);

    List<MedicineEntry> findByPondCyclePondIdOrderByCreatedAtDesc(
            UUID pondId);

    List<MedicineEntry> findByPondCyclePondId(
            UUID pondId);
}