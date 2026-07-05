package com.jala.backend.feeddelivery.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddSiteDeliveryRequest {

    @NotNull
    private UUID siteId;

    @NotNull
    @Min(1)
    private Integer numberOfBags;

    private String remarks;
}