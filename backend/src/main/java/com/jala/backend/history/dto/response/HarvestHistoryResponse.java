package com.jala.backend.history.dto.response;

import com.jala.backend.harvest.enums.HarvestStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class HarvestHistoryResponse {

    private UUID harvestId;

    private Integer cycleNumber;

    private LocalDate harvestDate;

    private BigDecimal harvestQuantityKg;

    private String buyerName;

    private BigDecimal sellingPricePerKg;

    private BigDecimal totalAmount;

    private String billPhotoPath;

    private HarvestStatus status;
}