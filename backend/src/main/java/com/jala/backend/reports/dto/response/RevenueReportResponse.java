package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RevenueReportResponse {

    private UUID siteId;
    private String siteCode;
    private String siteName;

    private UUID pondId;
    private String pondCode;
    private String pondName;

    private LocalDate fromDate;
    private LocalDate toDate;

    private Integer harvestCount;

    private BigDecimal totalHarvestKg;

    private BigDecimal averageHarvestKg;

    private BigDecimal averageSellingPrice;

    private BigDecimal totalRevenue;
}