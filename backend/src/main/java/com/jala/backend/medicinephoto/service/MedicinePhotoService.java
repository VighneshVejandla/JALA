package com.jala.backend.medicinephoto.service;

import com.jala.backend.medicinephoto.dto.request.CreateMedicinePhotoRequest;
import com.jala.backend.medicinephoto.dto.response.MedicinePhotoResponse;

import java.util.List;
import java.util.UUID;

public interface MedicinePhotoService {

    MedicinePhotoResponse uploadPhoto(
            CreateMedicinePhotoRequest request);

    List<MedicinePhotoResponse> getPhotos(
            UUID medicineEntryId);
}