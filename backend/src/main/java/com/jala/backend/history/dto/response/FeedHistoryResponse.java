package com.jala.backend.history.dto.response;

import com.jala.backend.feedentry.enums.FeedEntryStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class FeedHistoryResponse {

    private UUID feedEntryId;

    private Integer cycleNumber;

    private Integer sessionNumber;

    private LocalDate feedDate;

    private String feedSize;

    private BigDecimal feedQuantityKg;

    private String remarks;

    private FeedEntryStatus status;

    private String createdBy;
}