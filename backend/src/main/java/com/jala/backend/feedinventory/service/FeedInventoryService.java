package com.jala.backend.feedinventory.service;

import com.jala.backend.feedinventory.dto.response.FeedInventoryResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface FeedInventoryService {

    FeedInventoryResponse getInventoryBySite(
            UUID siteId);

    List<FeedInventoryResponse> getAllInventories();

    void increaseInventory(
            UUID siteId,
            BigDecimal quantityKg);

    void decreaseInventory(
            UUID siteId,
            BigDecimal quantityKg);

}