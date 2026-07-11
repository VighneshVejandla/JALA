package com.jala.backend.dashboard.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.dashboard.dto.response.HomeDashboardResponse;
import com.jala.backend.dashboard.dto.response.PondDashboardResponse;
import com.jala.backend.dashboard.service.PondDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.DASHBOARD_BASE_URL)
@RequiredArgsConstructor
public class PondDashboardController {

    private final PondDashboardService service;

    @GetMapping("/{pondId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PondDashboardResponse>> getDashboard(
            @PathVariable UUID pondId) {

        PondDashboardResponse response =
                service.getDashboard(pondId);

        return ResponseEntity.ok(
                ApiResponse.<PondDashboardResponse>builder()
                        .success(true)
                        .message("Pond dashboard fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/home/{siteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HomeDashboardResponse>>
    getHomeDashboard(
            @PathVariable UUID siteId) {

        HomeDashboardResponse response =
                service.getHomeDashboard(siteId);

        return ResponseEntity.ok(
                ApiResponse.<HomeDashboardResponse>builder()
                        .success(true)
                        .message("Home dashboard fetched successfully")
                        .data(response)
                        .build()
        );
    }
}