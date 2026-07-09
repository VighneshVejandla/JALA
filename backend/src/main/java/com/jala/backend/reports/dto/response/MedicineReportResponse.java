package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MedicineReportResponse {

    private UUID siteId;

    private String siteCode;

    private String siteName;

    private UUID pondId;

    private String pondCode;

    private String pondName;

    private LocalDate fromDate;

    private LocalDate toDate;

    private Integer medicineEntryCount;

    private BigDecimal totalMedicineQuantity;

    private List<MedicineReportItemResponse> details;
}