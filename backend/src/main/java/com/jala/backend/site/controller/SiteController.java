package com.jala.backend.site.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.site.dto.request.CreateSiteRequest;
import com.jala.backend.site.dto.request.UpdateSiteRequest;
import com.jala.backend.site.dto.response.SiteResponse;
import com.jala.backend.site.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.SITE_BASE_URL)
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteResponse>> createSite(
            @Valid @RequestBody CreateSiteRequest request) {

        SiteResponse response = siteService.createSite(request);

        return ResponseEntity.ok(
                ApiResponse.<SiteResponse>builder()
                        .success(true)
                        .message("Site created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getAllSites() {

        List<SiteResponse> response = siteService.getAllSites();

        return ResponseEntity.ok(
                ApiResponse.<List<SiteResponse>>builder()
                        .success(true)
                        .message("Sites fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteById(
            @PathVariable UUID id) {

        SiteResponse response = siteService.getSiteById(id);

        return ResponseEntity.ok(
                ApiResponse.<SiteResponse>builder()
                        .success(true)
                        .message("Site fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteResponse>> patchSite(
            @PathVariable UUID id,
            @RequestBody UpdateSiteRequest request) {

        SiteResponse response = siteService.patchSite(id, request);

        return ResponseEntity.ok(
                ApiResponse.<SiteResponse>builder()
                        .success(true)
                        .message("Site updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateSite(
            @PathVariable UUID id) {

        siteService.activateSite(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Site activated successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateSite(
            @PathVariable UUID id) {

        siteService.deactivateSite(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Site deactivated successfully")
                        .build()
        );
    }
}