package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SiteHarvestAnalyticsResponse {

    private UUID siteId;

    private String siteCode;

    private String siteName;

    private Integer harvestCount;

    private BigDecimal todayHarvestKg;

    private BigDecimal weekHarvestKg;

    private BigDecimal monthHarvestKg;

    private BigDecimal todayRevenue;

    private BigDecimal weekRevenue;

    private BigDecimal monthRevenue;
}