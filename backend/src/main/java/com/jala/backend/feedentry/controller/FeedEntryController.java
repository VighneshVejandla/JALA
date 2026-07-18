package com.jala.backend.feedentry.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.feedentry.dto.request.CancelFeedEntryRequest;
import com.jala.backend.feedentry.dto.request.CreateFeedEntryRequest;
import com.jala.backend.feedentry.dto.request.UpdateFeedEntryRequest;
import com.jala.backend.feedentry.dto.response.FeedEntryResponse;
import com.jala.backend.feedentry.service.FeedEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FEED_ENTRY_BASE_URL)
@RequiredArgsConstructor
public class FeedEntryController {

    private final FeedEntryService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<FeedEntryResponse>> createFeedEntry(
            @Valid @RequestBody CreateFeedEntryRequest request) {

        FeedEntryResponse response =
                service.createFeedEntry(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<FeedEntryResponse>builder()
                        .success(true)
                        .message("Feed entry created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FeedEntryResponse>>> getFeedEntries(
            @RequestParam UUID pondCycleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        List<FeedEntryResponse> response =
                service.getFeedEntries(
                        pondCycleId,
                        date);

        return ResponseEntity.ok(
                ApiResponse.<List<FeedEntryResponse>>builder()
                        .success(true)
                        .message("Feed entries fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<ApiResponse<FeedEntryResponse>> updateFeedEntry(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeedEntryRequest request) {

        FeedEntryResponse response =
                service.updateFeedEntry(id, request);

        return ResponseEntity.ok(
                ApiResponse.<FeedEntryResponse>builder()
                        .success(true)
                        .message("Feed entry updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelFeedEntry(
            @PathVariable UUID id,
            @Valid @RequestBody CancelFeedEntryRequest request) {

        service.cancelFeedEntry(
                id,
                request.getReason());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Feed entry cancelled successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreFeedEntry(
            @PathVariable UUID id) {

        service.restoreFeedEntry(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Feed entry restored successfully")
                        .build()
        );
    }

}