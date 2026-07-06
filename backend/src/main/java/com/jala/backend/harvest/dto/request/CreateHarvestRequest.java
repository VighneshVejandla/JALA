package com.jala.backend.harvest.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateHarvestRequest {

    @NotNull
    private UUID pondCycleId;

    @NotNull
    private LocalDate harvestDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal harvestQuantityKg;

    @NotNull
    private MultipartFile billPhoto;

    // -----------------------------
    // Optional Commercial Details
    // -----------------------------

    private String buyerName;

    @DecimalMin(value = "0.00")
    private BigDecimal sellingPricePerKg;

    private String vehicleNumber;

    private String remarks;
}