package com.jala.backend.feeddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeedDeliveryResponse {

    private UUID id;

    private String deliveredBy;

    private LocalDateTime deliveredAt;

    private String remarks;

    private String status;
}