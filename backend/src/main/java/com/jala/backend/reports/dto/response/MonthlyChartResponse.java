package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyChartResponse {

    private Integer month;

    private String monthName;

    private BigDecimal value;
}