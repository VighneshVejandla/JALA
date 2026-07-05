package com.jala.backend.feeddeliveryreceipt.dto.response;

import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SiteDeliveryReceiptResponse {

    private UUID id;

    private UUID siteDeliveryId;

    private String photoPath;

    private String remarks;

    private FeedDeliveryStatus status;

    private String uploadedByEmployeeCode;

    private LocalDateTime uploadedAt;
}