package com.jala.backend.feeddelivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SiteDeliveryResponse {

    private UUID id;

    private UUID siteId;

    private String siteCode;

    private String siteName;

    private Integer numberOfBags;

    private BigDecimal bagWeightKg;

    private BigDecimal totalKg;

    private String remarks;

    private String status;
}