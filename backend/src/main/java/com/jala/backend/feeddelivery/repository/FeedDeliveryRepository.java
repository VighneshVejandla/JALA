package com.jala.backend.feeddelivery.repository;

import com.jala.backend.feeddelivery.entity.FeedDelivery;
import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedDeliveryRepository
        extends JpaRepository<FeedDelivery, UUID> {

    List<FeedDelivery> findByStatusOrderByDeliveredAtDesc(
            FeedDeliveryStatus status);
}