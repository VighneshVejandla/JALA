package com.jala.backend.site.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSiteRequest {

    private String siteName;

    private String ownerName;

    private String location;

    private BigDecimal totalAcres;

    private Boolean isActive;
}