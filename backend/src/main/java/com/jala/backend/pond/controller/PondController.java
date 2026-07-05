package com.jala.backend.pond.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.pond.dto.request.CreatePondRequest;
import com.jala.backend.pond.dto.request.UpdatePondRequest;
import com.jala.backend.pond.dto.response.PondResponse;
import com.jala.backend.pond.service.PondService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.POND_BASE_URL)
@RequiredArgsConstructor
public class PondController {

    private final PondService pondService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PondResponse>> createPond(
            @Valid @RequestBody CreatePondRequest request) {

        PondResponse response = pondService.createPond(request);

        return ResponseEntity.ok(
                ApiResponse.<PondResponse>builder()
                        .success(true)
                        .message("Pond created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PondResponse>>> getAllPonds() {

        List<PondResponse> response = pondService.getAllPonds();

        return ResponseEntity.ok(
                ApiResponse.<List<PondResponse>>builder()
                        .success(true)
                        .message("Ponds fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PondResponse>> getPondById(
            @PathVariable UUID id) {

        PondResponse response = pondService.getPondById(id);

        return ResponseEntity.ok(
                ApiResponse.<PondResponse>builder()
                        .success(true)
                        .message("Pond fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PondResponse>>> getPondsBySite(
            @PathVariable UUID siteId) {

        List<PondResponse> response = pondService.getPondsBySite(siteId);

        return ResponseEntity.ok(
                ApiResponse.<List<PondResponse>>builder()
                        .success(true)
                        .message("Ponds fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PondResponse>> patchPond(
            @PathVariable UUID id,
            @RequestBody UpdatePondRequest request) {

        PondResponse response = pondService.patchPond(id, request);

        return ResponseEntity.ok(
                ApiResponse.<PondResponse>builder()
                        .success(true)
                        .message("Pond updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activatePond(
            @PathVariable UUID id) {

        pondService.activatePond(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Pond activated successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivatePond(
            @PathVariable UUID id) {

        pondService.deactivatePond(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Pond deactivated successfully")
                        .build()
        );
    }
}