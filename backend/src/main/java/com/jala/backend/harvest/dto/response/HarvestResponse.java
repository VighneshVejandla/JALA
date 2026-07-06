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

    private BigDecimal harvestQuantityKg;

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

    private String cancelledByEmployeeCode;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

}