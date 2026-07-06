package com.jala.backend.dashboard.dto.response;

import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PondDashboardResponse {

    // Pond Details
    private UUID pondId;

    private String pondCode;

    private String pondName;

    private UUID siteId;

    private String siteCode;

    private String siteName;

    // Active Cycle
    private UUID activeCycleId;

    private Integer cycleNumber;

    private PondCycleStatus cycleStatus;

    private Boolean stockingCompleted;

    private ShrimpSpecies species;

    private LocalDate stockingDate;

    private Long daysSinceStocking;

    private Integer shrimpCount;

    // Feed Summary

    private BigDecimal todayFeedKg;

    private BigDecimal totalFeedKg;

    private Integer todayFeedEntries;

    // Medicine Summary

    private Integer medicineEntryCount;

    private BigDecimal totalMedicineQuantity;

    private LocalDateTime lastMedicineDate;

    private Integer medicinePhotoCount;

    // Harvest Summary

    private Integer harvestCount;

    private LocalDate lastHarvestDate;

    private BigDecimal lastHarvestQuantityKg;

    private BigDecimal lastHarvestAmount;

    private String lastBuyerName;
}