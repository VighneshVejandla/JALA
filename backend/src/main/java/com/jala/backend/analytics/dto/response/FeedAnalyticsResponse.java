package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class FeedAnalyticsResponse {

    private UUID pondId;

    private String pondCode;

    private String pondName;

    // Feed Quantity
    private BigDecimal todayFeedKg;

    private BigDecimal weekFeedKg;

    private BigDecimal monthFeedKg;

    // Feed Entries
    private Integer todayFeedEntries;

    private Integer weekFeedEntries;

    private Integer monthFeedEntries;
}