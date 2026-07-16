package com.jala.backend.harvest.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.harvest.dto.request.CreateHarvestRequest;
import com.jala.backend.harvest.dto.request.CancelHarvestRequest;
import com.jala.backend.harvest.dto.response.HarvestResponse;
import com.jala.backend.harvest.service.HarvestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.HARVEST_BASE_URL)
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<HarvestResponse>> createHarvest(
            @Valid @ModelAttribute CreateHarvestRequest request) {

        HarvestResponse response =
                service.createHarvest(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<HarvestResponse>builder()
                        .success(true)
                        .message("Harvest created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<HarvestResponse>>> getHarvests(
            @RequestParam UUID pondCycleId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<HarvestResponse> response =
                service.getHarvests(pondCycleId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<List<HarvestResponse>>builder()
                        .success(true)
                        .message("Harvests fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{harvestId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<HarvestResponse>> cancelHarvest(
            @PathVariable UUID harvestId,
            @Valid @RequestBody CancelHarvestRequest request) {

        HarvestResponse response =
                service.cancelHarvest(
                        harvestId,
                        request);

        return ResponseEntity.ok(
                ApiResponse.<HarvestResponse>builder()
                        .success(true)
                        .message("Harvest cancelled successfully")
                        .data(response)
                        .build()
        );
    }
}