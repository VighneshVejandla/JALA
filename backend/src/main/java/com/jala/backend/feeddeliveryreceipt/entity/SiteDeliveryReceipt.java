package com.jala.backend.feeddeliveryreceipt.entity;

import com.jala.backend.feeddelivery.entity.SiteDelivery;
import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import com.jala.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "site_delivery_receipts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDeliveryReceipt {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_delivery_id", nullable = false)
    private SiteDelivery siteDelivery;

    @Column(name = "photo_path", nullable = false, length = 1000)
    private String photoPath;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedDeliveryStatus status;

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