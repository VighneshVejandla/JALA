package com.jala.backend.feedentry.dto.request;

import com.jala.backend.feedentry.enums.FeedSize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateFeedEntryRequest {

    private FeedSize feedSize;

    private BigDecimal feedQuantityKg;

    private String remarks;
}