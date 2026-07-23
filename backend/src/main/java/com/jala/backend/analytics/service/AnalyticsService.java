package com.jala.backend.analytics.service;

import com.jala.backend.analytics.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    FeedAnalyticsResponse getPondFeedAnalytics(
            UUID pondId);

    List<DailyFeedResponse> getSiteFeedDaily(
            UUID siteId,
            int days);

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