package com.jala.backend.history.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MedicineHistoryResponse {

    private UUID medicineId;

    private Integer cycleNumber;

    private BigDecimal quantity;

    private String unit;

    private String remarks;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private List<MedicineHistoryPhotoResponse> photos;
}