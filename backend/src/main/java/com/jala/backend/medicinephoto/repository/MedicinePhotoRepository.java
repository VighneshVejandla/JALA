package com.jala.backend.medicinephoto.repository;

import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MedicinePhotoRepository
        extends JpaRepository<MedicinePhoto, UUID> {

    @EntityGraph(attributePaths = {"medicineEntry", "uploadedBy"})
    List<MedicinePhoto> findByMedicineEntryIdOrderByUploadedAt(
            UUID medicineEntryId,
            Pageable pageable);

    /** Batch variant: loads photos for many entries in one query. */
    List<MedicinePhoto> findByMedicineEntryIdInOrderByUploadedAt(
            Collection<UUID> medicineEntryIds);

    long countByMedicineEntryId(
            UUID medicineEntryId);

    /**
     * Photo count across all ACTIVE medicine entries of a cycle in a
     * single aggregate, replacing the former count-per-entry loop.
     */
    @Query("""
            SELECT COUNT(p)
            FROM MedicinePhoto p
            WHERE p.medicineEntry.pondCycle.id = :pondCycleId
            AND p.medicineEntry.status =
                com.jala.backend.medicine.enums.MedicineStatus.ACTIVE
            """)
    long countByCycleWithActiveEntries(
            UUID pondCycleId);
}
