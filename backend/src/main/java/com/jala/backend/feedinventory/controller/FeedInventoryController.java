package com.jala.backend.feedinventory.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.feedinventory.dto.response.FeedInventoryResponse;
import com.jala.backend.feedinventory.service.FeedInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FEED_INVENTORY_BASE_URL)
@RequiredArgsConstructor
public class FeedInventoryController {

    private final FeedInventoryService service;

    @GetMapping("/{siteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FeedInventoryResponse>> getInventoryBySite(
            @PathVariable UUID siteId) {

        FeedInventoryResponse response =
                service.getInventoryBySite(siteId);

        return ResponseEntity.ok(
                ApiResponse.<FeedInventoryResponse>builder()
                        .success(true)
                        .message("Feed inventory fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FeedInventoryResponse>>> getAllInventories() {

        List<FeedInventoryResponse> response =
                service.getAllInventories();

        return ResponseEntity.ok(
                ApiResponse.<List<FeedInventoryResponse>>builder()
                        .success(true)
                        .message("Feed inventories fetched successfully")
                        .data(response)
                        .build()
        );
    }
}