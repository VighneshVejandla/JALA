package com.jala.backend.feedentry.dto.request;

import com.jala.backend.feedentry.enums.FeedSize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateFeedEntryRequest {

    @NotNull
    private UUID pondCycleId;

    @NotNull
    private UUID feedScheduleId;

    @NotNull
    private LocalDate feedDate;

    @NotNull
    private FeedSize feedSize;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal feedQuantityKg;

    private String remarks;
}