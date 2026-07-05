package com.jala.backend.feedinventory.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.feedinventory.dto.response.FeedInventoryResponse;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.mapper.FeedInventoryMapper;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedInventoryServiceImpl
        implements FeedInventoryService {

    private final FeedInventoryRepository repository;

    private final SiteRepository siteRepository;

    private final FeedInventoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public FeedInventoryResponse getInventoryBySite(UUID siteId) {

        FeedInventory inventory = repository.findBySiteId(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed inventory not found."));

        return mapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedInventoryResponse> getAllInventories() {

        return repository.findAllByOrderBySiteSiteCode()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void increaseInventory(
            UUID siteId,
            BigDecimal quantityKg) {

        FeedInventory inventory = repository
                .findBySiteId(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed inventory not found."));

        inventory.setTotalReceivedKg(
                inventory.getTotalReceivedKg()
                        .add(quantityKg));

        inventory.setAvailableKg(
                inventory.getAvailableKg()
                        .add(quantityKg));

        inventory.setUpdatedAt(LocalDateTime.now());

        repository.save(inventory);
    }

    @Override
    @Transactional
    public void decreaseInventory(
            UUID siteId,
            BigDecimal quantityKg) {

        FeedInventory inventory = repository
                .findBySiteId(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed inventory not found."));

        if (inventory.getAvailableKg()
                .compareTo(quantityKg) < 0) {

            throw new BadRequestException(
                    "Insufficient feed inventory.");
        }

        inventory.setTotalConsumedKg(
                inventory.getTotalConsumedKg()
                        .add(quantityKg));

        inventory.setAvailableKg(
                inventory.getAvailableKg()
                        .subtract(quantityKg));

        inventory.setUpdatedAt(LocalDateTime.now());

        repository.save(inventory);
    }

}