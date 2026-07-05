package com.jala.backend.pond.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PondResponse {

    private UUID id;

    private UUID siteId;

    private String siteName;

    private String pondCode;

    private String pondName;

    private BigDecimal pondAcres;

    private Boolean isActive;
}