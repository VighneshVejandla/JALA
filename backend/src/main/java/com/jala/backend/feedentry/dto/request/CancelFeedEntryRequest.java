package com.jala.backend.feedentry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelFeedEntryRequest {

    private String reason;
}