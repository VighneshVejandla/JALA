package com.jala.backend.medicine.service;

import com.jala.backend.medicine.dto.request.CreateMedicineRequest;
import com.jala.backend.medicine.dto.request.UpdateMedicineRequest;
import com.jala.backend.medicine.dto.response.MedicineResponse;

import java.util.List;
import java.util.UUID;

public interface MedicineService {

    MedicineResponse createMedicine(
            CreateMedicineRequest request);

    List<MedicineResponse> getMedicines(
            UUID pondCycleId);

    MedicineResponse updateMedicine(
            UUID id,
            UpdateMedicineRequest request);

    void cancelMedicine(
            UUID id,
            String reason);

    void restoreMedicine(
            UUID id);
}