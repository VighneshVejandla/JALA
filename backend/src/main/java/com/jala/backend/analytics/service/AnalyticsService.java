package com.jala.backend.analytics.service;

import com.jala.backend.analytics.dto.response.*;

import java.util.UUID;

public interface AnalyticsService {

    FeedAnalyticsResponse getPondFeedAnalytics(
            UUID pondId);

    SiteFeedAnalyticsResponse getSiteFeedAnalytics(
            UUID siteId);

    InventoryAnalyticsResponse getInventoryAnalytics(
            UUID siteId);

    PondHarvestAnalyticsResponse getPondHarvestAnalytics(
            UUID pondId);

    SiteHarvestAnalyticsResponse getSiteHarvestAnalytics(
            UUID siteId);

    AnalyticsDashboardResponse getAnalyticsDashboard(
            UUID siteId);

}