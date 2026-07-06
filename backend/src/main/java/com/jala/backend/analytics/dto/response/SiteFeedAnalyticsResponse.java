package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SiteFeedAnalyticsResponse {

    private UUID siteId;

    private String siteCode;

    private String siteName;

    // Feed Quantity
    private BigDecimal todayFeedKg;

    private BigDecimal weekFeedKg;

    private BigDecimal monthFeedKg;

    // Feed Entries
    private Integer todayFeedEntries;

    private Integer weekFeedEntries;

    private Integer monthFeedEntries;

    // Number of ponds fed
    private Integer pondsFedToday;

    private Integer pondsFedWeek;

    private Integer pondsFedMonth;
}