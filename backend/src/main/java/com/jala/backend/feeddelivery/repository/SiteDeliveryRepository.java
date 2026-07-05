package com.jala.backend.feeddelivery.repository;

import com.jala.backend.feeddelivery.entity.SiteDelivery;
import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SiteDeliveryRepository
        extends JpaRepository<SiteDelivery, UUID> {

    List<SiteDelivery> findByFeedDeliveryIdAndStatusOrderById(
            UUID feedDeliveryId,
            FeedDeliveryStatus status);

    List<SiteDelivery> findBySiteIdAndStatusOrderByIdDesc(
            UUID siteId,
            FeedDeliveryStatus status);
}