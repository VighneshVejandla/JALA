package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PondHarvestAnalyticsResponse {

    private UUID pondId;

    private String pondCode;

    private String pondName;

    private Integer harvestCount;

    private BigDecimal totalHarvestKg;

    private BigDecimal averageHarvestKg;

    private BigDecimal totalRevenue;

    private LocalDate lastHarvestDate;

    private BigDecimal lastHarvestQuantityKg;

    private BigDecimal lastHarvestRevenue;

    private String lastBuyer;
}