package com.jala.backend.analytics.controller;

import com.jala.backend.analytics.dto.response.FeedAnalyticsResponse;
import com.jala.backend.analytics.dto.response.SiteFeedAnalyticsResponse;
import com.jala.backend.analytics.dto.response.InventoryAnalyticsResponse;
import com.jala.backend.analytics.dto.response.PondHarvestAnalyticsResponse;
import com.jala.backend.analytics.dto.response.SiteHarvestAnalyticsResponse;
import com.jala.backend.analytics.dto.response.AnalyticsDashboardResponse;
import com.jala.backend.analytics.service.AnalyticsService;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ANALYTICS_BASE_URL)
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/feed/site/{siteId}/daily")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<java.util.List<
            com.jala.backend.analytics.dto.response.DailyFeedResponse>>>
    getSiteFeedDaily(
            @PathVariable UUID siteId,
            @RequestParam(required = false, defaultValue = "14") int days) {

        var response = service.getSiteFeedDaily(siteId, days);

        return ResponseEntity.ok(
                ApiResponse.<java.util.List<
                        com.jala.backend.analytics.dto.response.DailyFeedResponse>>
                        builder()
                        .success(true)
                        .message("Daily feed fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/feed/pond/{pondId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<FeedAnalyticsResponse>>
    getPondFeedAnalytics(
            @PathVariable UUID pondId) {

        FeedAnalyticsResponse response =
                service.getPondFeedAnalytics(pondId);

        return ResponseEntity.ok(
                ApiResponse.<FeedAnalyticsResponse>builder()
                        .success(true)
                        .message("Feed analytics fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/dashboard/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>>
    getAnalyticsDashboard(
            @PathVariable UUID siteId) {

        AnalyticsDashboardResponse response =
                service.getAnalyticsDashboard(
                        siteId);

        return ResponseEntity.ok(
                ApiResponse.<AnalyticsDashboardResponse>builder()

                        .success(true)

                        .message(
                                "Analytics dashboard fetched successfully")

                        .data(response)

                        .build());
    }

    @GetMapping("/feed/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<SiteFeedAnalyticsResponse>>
    getSiteFeedAnalytics(
            @PathVariable UUID siteId) {

        SiteFeedAnalyticsResponse response =
                service.getSiteFeedAnalytics(siteId);

        return ResponseEntity.ok(
                ApiResponse.<SiteFeedAnalyticsResponse>builder()
                        .success(true)
                        .message("Site feed analytics fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/inventory/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<InventoryAnalyticsResponse>>
    getInventoryAnalytics(
            @PathVariable UUID siteId) {

        InventoryAnalyticsResponse response =
                service.getInventoryAnalytics(siteId);

        return ResponseEntity.ok(
                ApiResponse.<InventoryAnalyticsResponse>builder()
                        .success(true)
                        .message("Inventory analytics fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/harvest/pond/{pondId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<PondHarvestAnalyticsResponse>>
    getPondHarvestAnalytics(
            @PathVariable UUID pondId) {

        PondHarvestAnalyticsResponse response =
                service.getPondHarvestAnalytics(
                        pondId);

        return ResponseEntity.ok(
                ApiResponse.<PondHarvestAnalyticsResponse>builder()
                        .success(true)
                        .message("Pond harvest analytics fetched successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/harvest/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<SiteHarvestAnalyticsResponse>>
    getSiteHarvestAnalytics(
            @PathVariable UUID siteId) {

        SiteHarvestAnalyticsResponse response =
                service.getSiteHarvestAnalytics(
                        siteId);

        return ResponseEntity.ok(
                ApiResponse.<SiteHarvestAnalyticsResponse>builder()
                        .success(true)
                        .message("Site harvest analytics fetched successfully")
                        .data(response)
                        .build());
    }

}