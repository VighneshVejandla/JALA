package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MedicineReportItemResponse {

    private LocalDateTime createdAt;

    private Integer cycleNumber;

    private BigDecimal quantity;

    private String unit;

    private String remarks;

    private String createdBy;

    private List<MedicinePhotoReportResponse> photos;
}