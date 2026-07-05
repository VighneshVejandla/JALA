package com.jala.backend.feeddelivery.entity;

import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import com.jala.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feed_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivered_by", nullable = false)
    private User deliveredBy;

    @Column(nullable = false)
    private LocalDateTime deliveredAt;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeedDeliveryStatus status =
            FeedDeliveryStatus.ACTIVE;

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