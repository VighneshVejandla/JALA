package com.jala.backend.harvest.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Editable fields of an existing harvest record. All fields are optional;
 * only non-null values are applied. The bill photo and pond cycle are not
 * editable here.
 */
@Data
public class UpdateHarvestRequest {

    private LocalDate harvestDate;

    @DecimalMin(value = "0.01")
    private BigDecimal harvestQuantityKg;

    private String buyerName;

    @DecimalMin(value = "0.00")
    private BigDecimal sellingPricePerKg;

    private String vehicleNumber;

    private String remarks;
}
