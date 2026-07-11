package com.jala.backend.export.service;

import com.jala.backend.export.util.ExcelExportUtil;
import com.jala.backend.export.util.PdfExportUtil;
import com.jala.backend.reports.dto.request.ReportFilterRequest;
import com.jala.backend.reports.dto.response.FeedReportResponse;
import com.jala.backend.reports.dto.response.MedicineReportResponse;
import com.jala.backend.reports.dto.response.RevenueReportResponse;
import com.jala.backend.reports.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl
        implements ExportService {

    private final ReportsService reportsService;

    @Override
    public byte[] exportRevenueExcel(
            ReportFilterRequest request) {

        RevenueReportResponse report =
                reportsService.getRevenueReport(request);

        return ExcelExportUtil.exportRevenue(report);
    }

    @Override
    public byte[] exportFeedExcel(
            ReportFilterRequest request) {

        FeedReportResponse report =
                reportsService.getFeedReport(request);

        return ExcelExportUtil.exportFeed(report);
    }

    @Override
    public byte[] exportMedicineExcel(
            ReportFilterRequest request) {

        MedicineReportResponse report =
                reportsService.getMedicineReport(request);

        return ExcelExportUtil.exportMedicine(report);
    }

    @Override
    public byte[] exportRevenuePdf(
            ReportFilterRequest request) {

        return PdfExportUtil.exportRevenue(
                reportsService.getRevenueReport(request));
    }

    @Override
    public byte[] exportFeedPdf(
            ReportFilterRequest request) {

        return PdfExportUtil.exportFeed(
                reportsService.getFeedReport(request));
    }

    @Override
    public byte[] exportMedicinePdf(
            ReportFilterRequest request) {

        return PdfExportUtil.exportMedicine(
                reportsService.getMedicineReport(request));
    }
}