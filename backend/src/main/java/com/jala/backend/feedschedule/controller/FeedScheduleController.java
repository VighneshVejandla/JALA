package com.jala.backend.feedschedule.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.feedschedule.dto.request.CreateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.request.UpdateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.response.FeedScheduleResponse;
import com.jala.backend.feedschedule.service.FeedScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FEED_SCHEDULE_BASE_URL)
@RequiredArgsConstructor
public class FeedScheduleController {

    private final FeedScheduleService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedScheduleResponse>>> createSchedules(
            @Valid @RequestBody CreateFeedScheduleRequest request) {

        List<FeedScheduleResponse> response =
                service.createSchedules(request);

        return ResponseEntity.ok(
                ApiResponse.<List<FeedScheduleResponse>>builder()
                        .success(true)
                        .message("Feed schedules created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/cycle/{cycleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FeedScheduleResponse>>> getSchedules(
            @PathVariable UUID cycleId) {

        List<FeedScheduleResponse> response =
                service.getSchedulesByCycle(cycleId);

        return ResponseEntity.ok(
                ApiResponse.<List<FeedScheduleResponse>>builder()
                        .success(true)
                        .message("Feed schedules fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedScheduleResponse>> updateSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeedScheduleRequest request) {

        FeedScheduleResponse response =
                service.updateSchedule(id, request);

        return ResponseEntity.ok(
                ApiResponse.<FeedScheduleResponse>builder()
                        .success(true)
                        .message("Feed schedule updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateSchedule(
            @PathVariable UUID id) {

        service.activateSchedule(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Feed schedule activated successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateSchedule(
            @PathVariable UUID id) {

        service.deactivateSchedule(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Feed schedule deactivated successfully")
                        .build()
        );
    }
}