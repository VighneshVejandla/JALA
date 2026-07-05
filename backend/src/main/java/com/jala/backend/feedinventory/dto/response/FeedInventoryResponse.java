package com.jala.backend.feedinventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class FeedInventoryResponse {

    private UUID id;

    private UUID siteId;

    private String siteCode;

    private String siteName;

    private BigDecimal totalReceivedKg;

    private BigDecimal totalConsumedKg;

    private BigDecimal availableKg;
}