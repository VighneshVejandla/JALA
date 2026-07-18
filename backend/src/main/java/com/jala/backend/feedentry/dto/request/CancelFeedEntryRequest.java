package com.jala.backend.feedentry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelFeedEntryRequest {

    @NotBlank
    @Size(max = 500)
    private String reason;
}
