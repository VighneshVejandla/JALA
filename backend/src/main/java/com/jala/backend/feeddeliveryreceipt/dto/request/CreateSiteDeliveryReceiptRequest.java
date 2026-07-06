package com.jala.backend.feeddeliveryreceipt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class CreateSiteDeliveryReceiptRequest {

    @NotNull
    private UUID siteDeliveryId;

    @NotNull
    private MultipartFile file;

    private String remarks;
}