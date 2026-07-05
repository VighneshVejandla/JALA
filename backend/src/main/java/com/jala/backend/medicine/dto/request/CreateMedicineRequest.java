package com.jala.backend.medicine.dto.request;

import com.jala.backend.medicine.enums.MedicineUnit;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateMedicineRequest {

    private UUID pondCycleId;

    private BigDecimal quantity;

    private MedicineUnit unit;

    private String remarks;
}