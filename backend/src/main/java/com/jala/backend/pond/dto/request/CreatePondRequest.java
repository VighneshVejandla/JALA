package com.jala.backend.pond.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePondRequest {

    @NotNull
    private UUID siteId;

    @NotBlank
    private String pondCode;

    @NotBlank
    private String pondName;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal pondAcres;
}