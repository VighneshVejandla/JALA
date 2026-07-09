package com.jala.backend.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChartsResponse {

    private List<MonthlyChartResponse> revenue;

    private List<MonthlyChartResponse> feed;

    private List<MonthlyChartResponse> harvest;
}