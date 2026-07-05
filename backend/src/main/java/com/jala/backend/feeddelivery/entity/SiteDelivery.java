package com.jala.backend.feeddelivery.entity;

import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import com.jala.backend.site.entity.Site;
import com.jala.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "site_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_delivery_id", nullable = false)
    private FeedDelivery feedDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private Integer numberOfBags;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bagWeightKg;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalKg;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeedDeliveryStatus status = FeedDeliveryStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restored_by")
    private User restoredBy;

    private LocalDateTime restoredAt;

    @Column(length = 500)
    private String restorationReason;
}