package com.jala.backend.pond.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePondRequest {

    private String pondCode;

    private String pondName;

    private BigDecimal pondAcres;

    private Boolean isActive;
}