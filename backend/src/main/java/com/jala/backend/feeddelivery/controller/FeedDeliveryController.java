package com.jala.backend.feeddelivery.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.feeddelivery.dto.request.AddSiteDeliveryRequest;
import com.jala.backend.feeddelivery.dto.request.CreateFeedDeliveryRequest;
import com.jala.backend.feeddelivery.dto.response.FeedDeliveryResponse;
import com.jala.backend.feeddelivery.dto.response.SiteDeliveryResponse;
import com.jala.backend.feeddelivery.service.FeedDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FEED_DELIVERY_BASE_URL)
@RequiredArgsConstructor
public class FeedDeliveryController {

    private final FeedDeliveryService feedDeliveryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    public ResponseEntity<ApiResponse<FeedDeliveryResponse>> createDelivery(
            @Valid @RequestBody CreateFeedDeliveryRequest request) {

        FeedDeliveryResponse response =
                feedDeliveryService.createDelivery(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<FeedDeliveryResponse>builder()
                        .success(true)
                        .message("Feed delivery created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR','DRIVER')")
    public ResponseEntity<ApiResponse<List<FeedDeliveryResponse>>> getAllDeliveries(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<FeedDeliveryResponse> response =
                feedDeliveryService.getAllDeliveries(page, size);

        return ResponseEntity.ok(
                ApiResponse.<List<FeedDeliveryResponse>>builder()
                        .success(true)
                        .message("Feed deliveries fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR','DRIVER')")
    public ResponseEntity<ApiResponse<FeedDeliveryResponse>> getDelivery(
            @PathVariable UUID id) {

        FeedDeliveryResponse response =
                feedDeliveryService.getDelivery(id);

        return ResponseEntity.ok(
                ApiResponse.<FeedDeliveryResponse>builder()
                        .success(true)
                        .message("Feed delivery fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/{deliveryId}/sites")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    public ResponseEntity<ApiResponse<SiteDeliveryResponse>> addSiteDelivery(
            @PathVariable UUID deliveryId,
            @Valid @RequestBody AddSiteDeliveryRequest request) {

        SiteDeliveryResponse response =
                feedDeliveryService.addSiteDelivery(
                        deliveryId,
                        request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SiteDeliveryResponse>builder()
                        .success(true)
                        .message("Site delivery added successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{deliveryId}/sites")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR','DRIVER')")
    public ResponseEntity<ApiResponse<List<SiteDeliveryResponse>>> getSiteDeliveries(
            @PathVariable UUID deliveryId) {

        List<SiteDeliveryResponse> response =
                feedDeliveryService.getSiteDeliveries(deliveryId);

        return ResponseEntity.ok(
                ApiResponse.<List<SiteDeliveryResponse>>builder()
                        .success(true)
                        .message("Site deliveries fetched successfully")
                        .data(response)
                        .build()
        );
    }
}