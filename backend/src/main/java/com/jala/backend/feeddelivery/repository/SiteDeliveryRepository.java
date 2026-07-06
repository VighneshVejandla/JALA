package com.jala.backend.feeddelivery.repository;

import com.jala.backend.feeddelivery.entity.SiteDelivery;
import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Query("""
SELECT COALESCE(SUM(s.totalKg),0)
FROM SiteDelivery s
WHERE s.site.id=:siteId
AND DATE(s.feedDelivery.deliveredAt)=:date
AND s.status=com.jala.backend.feeddelivery.enums.FeedDeliveryStatus.ACTIVE
""")
    BigDecimal getDeliveredForDate(
            UUID siteId,
            LocalDate date);

    @Query("""
SELECT COALESCE(SUM(s.totalKg),0)
FROM SiteDelivery s
WHERE s.site.id=:siteId
AND DATE(s.feedDelivery.deliveredAt)
BETWEEN :startDate AND :endDate
AND s.status=com.jala.backend.feeddelivery.enums.FeedDeliveryStatus.ACTIVE
""")
    BigDecimal getDeliveredBetweenDates(
            UUID siteId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
SELECT COALESCE(SUM(s.totalKg),0)
FROM SiteDelivery s
WHERE s.site.id=:siteId
AND s.status=com.jala.backend.feeddelivery.enums.FeedDeliveryStatus.ACTIVE
""")
    BigDecimal getTotalDelivered(
            UUID siteId);


}