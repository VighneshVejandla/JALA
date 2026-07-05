package com.jala.backend.medicine.dto.request;

import com.jala.backend.medicine.enums.MedicineUnit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMedicineRequest {

    private BigDecimal quantity;

    private MedicineUnit unit;

    private String remarks;
}