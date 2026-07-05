package com.jala.backend.feeddeliveryreceipt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelSiteDeliveryReceiptRequest {

    @NotBlank
    private String cancellationReason;
}