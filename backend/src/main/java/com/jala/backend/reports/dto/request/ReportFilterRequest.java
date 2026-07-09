package com.jala.backend.reports.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ReportFilterRequest {

    @NotNull
    private UUID siteId;

    // Optional
    private UUID pondId;

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;
}