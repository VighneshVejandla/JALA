package com.jala.backend.site.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSiteRequest {

    @NotBlank
    private String siteCode;

    @NotBlank
    private String siteName;

    @NotBlank
    private String ownerName;

    @NotBlank
    private String location;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal totalAcres;
}