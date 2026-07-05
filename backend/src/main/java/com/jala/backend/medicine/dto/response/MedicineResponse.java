package com.jala.backend.medicine.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MedicineResponse {

    private UUID id;

    private UUID pondCycleId;

    private BigDecimal quantity;

    private String unit;

    private String remarks;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;
}