package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class InventoryAnalyticsResponse {

    private UUID siteId;

    private String siteCode;

    private String siteName;

    // Delivery
    private BigDecimal deliveredTodayKg;
    private BigDecimal deliveredWeekKg;
    private BigDecimal deliveredMonthKg;
    private BigDecimal totalDeliveredKg;

    // Consumption
    private BigDecimal consumedTodayKg;
    private BigDecimal consumedWeekKg;
    private BigDecimal consumedMonthKg;
    private BigDecimal totalConsumedKg;

    // Current Stock
    private BigDecimal availableKg;

    private Integer availableBags;
}