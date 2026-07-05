package com.jala.backend.medicine.repository;

import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicineRepository
        extends JpaRepository<MedicineEntry, UUID> {

    List<MedicineEntry> findByPondCycleIdAndStatusOrderByCreatedAtDesc(
            UUID pondCycleId,
            MedicineStatus status);
}