package com.jala.backend.reports.service;

import com.jala.backend.reports.dto.request.ReportFilterRequest;
import com.jala.backend.reports.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface ReportsService {

    RevenueReportResponse getRevenueReport(
            ReportFilterRequest request);

    FeedReportResponse getFeedReport(
            ReportFilterRequest request);

    List<MonthlyChartResponse> getRevenueChart(
            UUID siteId);

    List<MonthlyChartResponse> getFeedChart(
            UUID siteId);

    List<MonthlyChartResponse> getHarvestChart(
            UUID siteId);

    ReportsDashboardResponse getDashboard(
            UUID siteId);

    MedicineReportResponse getMedicineReport(
            ReportFilterRequest request);
}