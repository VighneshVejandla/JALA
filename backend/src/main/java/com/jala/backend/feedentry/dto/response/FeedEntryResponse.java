package com.jala.backend.feedentry.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class FeedEntryResponse {

    private UUID id;

    private UUID pondCycleId;

    private UUID feedScheduleId;

    private Integer sessionNumber;

    private LocalDate feedDate;

    private String feedSize;

    private BigDecimal feedQuantityKg;

    private String remarks;

    private String createdBy;
}