package com.jala.backend.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AnalyticsDashboardResponse {

    private UUID siteId;

    private String siteCode;

    private String siteName;

    private SiteFeedAnalyticsResponse feed;

    private InventoryAnalyticsResponse inventory;

    private SiteHarvestAnalyticsResponse harvest;
}