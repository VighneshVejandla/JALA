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
        SELECT MAX(m.createdAt)
        FROM MedicineEntry m
        WHERE m.pondCycle.id = :pondCycleId
        AND m.status = com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
        """)
    LocalDateTime getLastMedicineDate(
            UUID pondCycleId);
}