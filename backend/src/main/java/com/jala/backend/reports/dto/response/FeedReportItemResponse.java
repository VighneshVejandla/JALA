package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FeedReportItemResponse {

    private LocalDate feedDate;

    private Integer cycleNumber;

    private Integer sessionNumber;

    private String feedSize;

    private BigDecimal feedQuantityKg;

    private String remarks;

    private String createdBy;
}