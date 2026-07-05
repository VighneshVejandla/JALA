package com.jala.backend.feeddeliveryreceipt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSiteDeliveryReceiptRequest {

    @NotNull
    private UUID siteDeliveryId;

    @NotBlank
    private String photoPath;

    private String remarks;
}