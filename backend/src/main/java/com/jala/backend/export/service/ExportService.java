package com.jala.backend.export.service;

import com.jala.backend.reports.dto.request.ReportFilterRequest;

public interface ExportService {

    byte[] exportRevenueExcel(
            ReportFilterRequest request);

    byte[] exportFeedExcel(
            ReportFilterRequest request);

    byte[] exportMedicineExcel(
            ReportFilterRequest request);

    byte[] exportRevenuePdf(
            ReportFilterRequest request);

    byte[] exportFeedPdf(
            ReportFilterRequest request);

    byte[] exportMedicinePdf(
            ReportFilterRequest request);
}