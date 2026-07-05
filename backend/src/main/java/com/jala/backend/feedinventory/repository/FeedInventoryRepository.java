package com.jala.backend.feedinventory.repository;

import com.jala.backend.feedinventory.entity.FeedInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedInventoryRepository
        extends JpaRepository<FeedInventory, UUID> {

    Optional<FeedInventory> findBySiteId(UUID siteId);

    List<FeedInventory> findAllByOrderBySiteSiteCode();
}