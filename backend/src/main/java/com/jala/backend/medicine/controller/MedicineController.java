package com.jala.backend.medicine.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.medicine.dto.request.CancelMedicineRequest;
import com.jala.backend.medicine.dto.request.CreateMedicineRequest;
import com.jala.backend.medicine.dto.request.UpdateMedicineRequest;
import com.jala.backend.medicine.dto.response.MedicineResponse;
import com.jala.backend.medicine.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.MEDICINE_BASE_URL)
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<MedicineResponse>> createMedicine(
            @Valid @RequestBody CreateMedicineRequest request) {

        MedicineResponse response =
                service.createMedicine(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<MedicineResponse>builder()
                        .success(true)
                        .message("Medicine entry created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getMedicines(
            @RequestParam UUID pondCycleId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<MedicineResponse> response =
                service.getMedicines(pondCycleId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<List<MedicineResponse>>builder()
                        .success(true)
                        .message("Medicine entries fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateMedicine(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicineRequest request) {

        MedicineResponse response =
                service.updateMedicine(id, request);

        return ResponseEntity.ok(
                ApiResponse.<MedicineResponse>builder()
                        .success(true)
                        .message("Medicine entry updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelMedicine(
            @PathVariable UUID id,
            @Valid @RequestBody CancelMedicineRequest request) {

        service.cancelMedicine(id, request.getReason());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Medicine entry cancelled successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreMedicine(
            @PathVariable UUID id) {

        service.restoreMedicine(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Medicine entry restored successfully")
                        .build()
        );
    }
}