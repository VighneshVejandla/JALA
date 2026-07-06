package com.jala.backend.dashboard.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.dashboard.dto.response.PondDashboardResponse;
import com.jala.backend.dashboard.service.PondDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}