package com.jala.backend.history.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.history.dto.response.*;
import com.jala.backend.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.HISTORY_BASE_URL)
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService service;

    @GetMapping("/pond/{pondId}/cycles")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<PondCycleHistoryResponse>>>
    getPondCycleHistory(
            @PathVariable UUID pondId) {

        List<PondCycleHistoryResponse> response =
                service.getPondCycleHistory(pondId);

        return ResponseEntity.ok(
                ApiResponse.<List<PondCycleHistoryResponse>>builder()
                        .success(true)
                        .message("Pond cycle history fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/pond/{pondId}/harvests")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<HarvestHistoryResponse>>>
    getHarvestHistory(
            @PathVariable UUID pondId) {

        List<HarvestHistoryResponse> response =
                service.getHarvestHistory(pondId);

        return ResponseEntity.ok(
                ApiResponse.<List<HarvestHistoryResponse>>builder()
                        .success(true)
                        .message("Harvest history fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/pond/{pondId}/feeds")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<FeedHistoryResponse>>>
    getFeedHistory(
            @PathVariable UUID pondId) {

        List<FeedHistoryResponse> response =
                service.getFeedHistory(pondId);

        return ResponseEntity.ok(
                ApiResponse.<List<FeedHistoryResponse>>builder()
                        .success(true)
                        .message("Feed history fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/pond/{pondId}/medicines")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<List<MedicineHistoryResponse>>>
    getMedicineHistory(
            @PathVariable UUID pondId) {

        List<MedicineHistoryResponse> response =
                service.getMedicineHistory(pondId);

        return ResponseEntity.ok(
                ApiResponse.<List<MedicineHistoryResponse>>builder()
                        .success(true)
                        .message("Medicine history fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/pond/{pondId}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<PondTimelineResponse>>
    getTimeline(
            @PathVariable UUID pondId) {

        PondTimelineResponse response =
                service.getTimeline(pondId);

        return ResponseEntity.ok(
                ApiResponse.<PondTimelineResponse>builder()
                        .success(true)
                        .message("Pond timeline fetched successfully")
                        .data(response)
                        .build()
        );
    }
}