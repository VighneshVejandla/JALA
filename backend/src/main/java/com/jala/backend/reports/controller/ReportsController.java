package com.jala.backend.reports.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.reports.dto.request.ReportFilterRequest;
import com.jala.backend.reports.dto.response.*;
import com.jala.backend.reports.service.ReportsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.REPORTS_BASE_URL)
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService service;

    @PostMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<RevenueReportResponse>>
    getRevenueReport(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        RevenueReportResponse response =
                service.getRevenueReport(request);

        return ResponseEntity.ok(
                ApiResponse.<RevenueReportResponse>builder()
                        .success(true)
                        .message("Revenue report generated successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/feed")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<FeedReportResponse>>
    getFeedReport(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        FeedReportResponse response =
                service.getFeedReport(request);

        return ResponseEntity.ok(
                ApiResponse.<FeedReportResponse>builder()
                        .success(true)
                        .message("Feed report generated successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/chart/revenue/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<MonthlyChartResponse>>>
    getRevenueChart(
            @PathVariable UUID siteId) {

        return ResponseEntity.ok(
                ApiResponse.<List<MonthlyChartResponse>>builder()
                        .success(true)
                        .message("Revenue chart generated successfully")
                        .data(service.getRevenueChart(siteId))
                        .build());
    }

    @GetMapping("/chart/feed/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<MonthlyChartResponse>>>
    getFeedChart(
            @PathVariable UUID siteId) {

        return ResponseEntity.ok(
                ApiResponse.<List<MonthlyChartResponse>>builder()
                        .success(true)
                        .message("Feed chart generated successfully")
                        .data(service.getFeedChart(siteId))
                        .build());
    }

    @GetMapping("/chart/harvest/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<MonthlyChartResponse>>>
    getHarvestChart(
            @PathVariable UUID siteId) {

        return ResponseEntity.ok(
                ApiResponse.<List<MonthlyChartResponse>>builder()
                        .success(true)
                        .message("Harvest chart generated successfully")
                        .data(service.getHarvestChart(siteId))
                        .build());
    }

    @GetMapping("/dashboard/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<ReportsDashboardResponse>>
    getDashboard(
            @PathVariable UUID siteId) {

        ReportsDashboardResponse response =
                service.getDashboard(siteId);

        return ResponseEntity.ok(
                ApiResponse.<ReportsDashboardResponse>builder()
                        .success(true)
                        .message("Reports dashboard fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/medicine")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<MedicineReportResponse>>
    getMedicineReport(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        MedicineReportResponse response =
                service.getMedicineReport(request);

        return ResponseEntity.ok(
                ApiResponse.<MedicineReportResponse>builder()
                        .success(true)
                        .message("Medicine report generated successfully")
                        .data(response)
                        .build()
        );
    }
}