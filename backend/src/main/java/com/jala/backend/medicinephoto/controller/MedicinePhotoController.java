package com.jala.backend.medicinephoto.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.medicinephoto.dto.request.CreateMedicinePhotoRequest;
import com.jala.backend.medicinephoto.dto.response.MedicinePhotoResponse;
import com.jala.backend.medicinephoto.service.MedicinePhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.MEDICINE_PHOTO_BASE_URL)
@RequiredArgsConstructor
public class MedicinePhotoController {

    private final MedicinePhotoService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<MedicinePhotoResponse>> uploadPhoto(
            @Valid @ModelAttribute CreateMedicinePhotoRequest request) {

        MedicinePhotoResponse response =
                service.uploadPhoto(request);

        return ResponseEntity.ok(
                ApiResponse.<MedicinePhotoResponse>builder()
                        .success(true)
                        .message("Medicine photo uploaded successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MedicinePhotoResponse>>> getPhotos(
            @RequestParam UUID medicineEntryId) {

        List<MedicinePhotoResponse> response =
                service.getPhotos(medicineEntryId);

        return ResponseEntity.ok(
                ApiResponse.<List<MedicinePhotoResponse>>builder()
                        .success(true)
                        .message("Medicine photos fetched successfully")
                        .data(response)
                        .build()
        );
    }
}