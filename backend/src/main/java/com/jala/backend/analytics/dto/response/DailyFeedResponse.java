package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One day's total feed for a site (used for the daily feed trend series). */
@Data
@Builder
public class DailyFeedResponse {

    private LocalDate date;

    private BigDecimal feedKg;
}
