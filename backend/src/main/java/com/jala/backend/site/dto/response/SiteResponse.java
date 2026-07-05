package com.jala.backend.site.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SiteResponse {

    private UUID id;

    private String siteCode;

    private String siteName;

    private String ownerName;

    private String location;

    private BigDecimal totalAcres;

    private Boolean isActive;
}