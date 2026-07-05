package com.jala.backend.feeddeliveryreceipt.repository;

import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import com.jala.backend.feeddeliveryreceipt.entity.SiteDeliveryReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SiteDeliveryReceiptRepository
        extends JpaRepository<SiteDeliveryReceipt, UUID> {

    List<SiteDeliveryReceipt>
    findBySiteDeliveryIdAndStatusOrderByUploadedAtAsc(
            UUID siteDeliveryId,
            FeedDeliveryStatus status);
}