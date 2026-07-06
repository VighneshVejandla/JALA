package com.jala.backend.feeddeliveryreceipt.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.feeddeliveryreceipt.dto.request.CancelSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.request.CreateSiteDeliveryReceiptRequest;
import com.jala.backend.feeddeliveryreceipt.dto.response.SiteDeliveryReceiptResponse;
import com.jala.backend.feeddeliveryreceipt.service.SiteDeliveryReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.SITE_DELIVERY_RECEIPT_BASE_URL)
@RequiredArgsConstructor
public class SiteDeliveryReceiptController {

    private final SiteDeliveryReceiptService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    public ResponseEntity<ApiResponse<SiteDeliveryReceiptResponse>> uploadReceipt(
            @Valid @ModelAttribute CreateSiteDeliveryReceiptRequest request) {

        SiteDeliveryReceiptResponse response =
                service.uploadReceipt(request);

        return ResponseEntity.ok(
                ApiResponse.<SiteDeliveryReceiptResponse>builder()
                        .success(true)
                        .message("Receipt uploaded successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SiteDeliveryReceiptResponse>>> getReceipts(
            @RequestParam UUID siteDeliveryId) {

        List<SiteDeliveryReceiptResponse> response =
                service.getReceipts(siteDeliveryId);

        return ResponseEntity.ok(
                ApiResponse.<List<SiteDeliveryReceiptResponse>>builder()
                        .success(true)
                        .message("Receipts fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{receiptId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelReceipt(
            @PathVariable UUID receiptId,
            @Valid @RequestBody CancelSiteDeliveryReceiptRequest request) {

        service.cancelReceipt(receiptId, request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Receipt cancelled successfully")
                        .build()
        );
    }

    @PatchMapping("/{receiptId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreReceipt(
            @PathVariable UUID receiptId) {

        service.restoreReceipt(receiptId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Receipt restored successfully")
                        .build()
        );
    }
}