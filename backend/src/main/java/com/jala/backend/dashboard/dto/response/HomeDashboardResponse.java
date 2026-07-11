package com.jala.backend.dashboard.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeDashboardResponse {

    // Site Details

    private UUID siteId;

    private String siteCode;

    private String siteName;

    // Ponds

    private Long totalPonds;

    private Long activeCycles;

    // Feed

    private BigDecimal todayFeedKg;

    private BigDecimal availableFeedKg;

    // Harvest

    private BigDecimal todayHarvestKg;

    private BigDecimal todayRevenue;

    // Notifications

    private Long unreadNotifications;

    // Inventory

    private Boolean lowInventory;
}