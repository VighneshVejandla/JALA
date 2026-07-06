package com.jala.backend.harvest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelHarvestRequest {

    @NotBlank
    private String cancellationReason;

}