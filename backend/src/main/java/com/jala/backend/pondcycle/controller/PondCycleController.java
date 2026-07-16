package com.jala.backend.pondcycle.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.pondcycle.dto.request.CreatePondCycleRequest;
import com.jala.backend.pondcycle.dto.request.UpdatePondCycleRequest;
import com.jala.backend.pondcycle.dto.response.PondCycleResponse;
import com.jala.backend.pondcycle.service.PondCycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.POND_CYCLE_BASE_URL)
@RequiredArgsConstructor
public class PondCycleController {

    private final PondCycleService pondCycleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PondCycleResponse>> createCycle(
            @Valid @RequestBody CreatePondCycleRequest request) {

        PondCycleResponse response = pondCycleService.createCycle(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<PondCycleResponse>builder()
                        .success(true)
                        .message("Pond cycle created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/active/{pondId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PondCycleResponse>> getActiveCycle(
            @PathVariable UUID pondId) {

        PondCycleResponse response = pondCycleService.getActiveCycle(pondId);

        return ResponseEntity.ok(
                ApiResponse.<PondCycleResponse>builder()
                        .success(true)
                        .message("Active cycle fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/pond/{pondId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PondCycleResponse>>> getCyclesByPond(
            @PathVariable UUID pondId) {

        List<PondCycleResponse> response =
                pondCycleService.getCyclesByPond(pondId);

        return ResponseEntity.ok(
                ApiResponse.<List<PondCycleResponse>>builder()
                        .success(true)
                        .message("Pond cycle history fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PondCycleResponse>> updateCycle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePondCycleRequest request) {

        PondCycleResponse response =
                pondCycleService.updateCycle(id, request);

        return ResponseEntity.ok(
                ApiResponse.<PondCycleResponse>builder()
                        .success(true)
                        .message("Pond cycle updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/harvest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> harvestCycle(
            @PathVariable UUID id) {

        pondCycleService.harvestCycle(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Pond harvested successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/undo-harvest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> undoHarvest(
            @PathVariable UUID id) {

        pondCycleService.undoHarvest(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Harvest undone successfully")
                        .build()
        );
    }
}