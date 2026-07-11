package com.jala.backend.export.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.export.service.ExportService;
import com.jala.backend.reports.dto.request.ReportFilterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.EXPORT_BASE_URL)
@RequiredArgsConstructor
public class ExportController {

    private final ExportService service;

    @PostMapping("/revenue/excel")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<byte[]> exportRevenue(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        byte[] file = service.exportRevenueExcel(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Revenue_Report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/feed/excel")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<byte[]> exportFeed(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        byte[] file = service.exportFeedExcel(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Feed_Report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/medicine/excel")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<byte[]> exportMedicine(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        byte[] file = service.exportMedicineExcel(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Medicine_Report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/revenue/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<byte[]> exportRevenuePdf(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        byte[] file = service.exportRevenuePdf(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Revenue_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @PostMapping("/feed/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<byte[]> exportFeedPdf(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        byte[] file = service.exportFeedPdf(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Feed_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @PostMapping("/medicine/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    public ResponseEntity<byte[]> exportMedicinePdf(
            @Valid
            @RequestBody
            ReportFilterRequest request) {

        byte[] file = service.exportMedicinePdf(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Medicine_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

}