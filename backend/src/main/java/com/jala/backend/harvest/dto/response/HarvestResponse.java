package com.jala.backend.harvest.dto.response;

import com.jala.backend.harvest.enums.HarvestStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HarvestResponse {

    private UUID id;

    private UUID pondCycleId;

    private LocalDate harvestDate;

    /**
     * Always returned in KG.
     */
    private BigDecimal harvestQuantityKg;

    /**
     * Display value for UI.
     * Example:
     * 850 KG
     * 1.25 Tons (1250 KG)
     */
    private String quantityDisplay;

    private String billPhotoPath;

    private String buyerName;

    private BigDecimal sellingPricePerKg;

    private BigDecimal totalAmount;

    private String vehicleNumber;

    private String remarks;

    private HarvestStatus status;

    private String uploadedByEmployeeCode;

    private LocalDateTime uploadedAt;

}