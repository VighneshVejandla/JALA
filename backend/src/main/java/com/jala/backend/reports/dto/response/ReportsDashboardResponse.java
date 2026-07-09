package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReportsDashboardResponse {

    private UUID siteId;

    private String siteCode;

    private String siteName;

    private RevenueReportResponse revenue;

    private FeedReportResponse feed;

    private MedicineReportResponse medicine;

    private ChartsResponse charts;
}