package com.jala.backend.medicinephoto.repository;

import com.jala.backend.medicinephoto.entity.MedicinePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicinePhotoRepository
        extends JpaRepository<MedicinePhoto, UUID> {

    List<MedicinePhoto> findByMedicineEntryIdOrderByUploadedAt(
            UUID medicineEntryId);

    long countByMedicineEntryId(
            UUID medicineEntryId);
}