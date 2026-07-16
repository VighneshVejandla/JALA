package com.jala.backend.feedentry.entity;

import com.jala.backend.common.entity.BaseEntity;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import com.jala.backend.feedentry.enums.FeedSize;
import com.jala.backend.feedschedule.entity.FeedSchedule;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feed_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pond_cycle_id", nullable = false)
    private PondCycle pondCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_schedule_id", nullable = false)
    private FeedSchedule feedSchedule;

    @Column(name = "feed_date", nullable = false)
    private LocalDate feedDate;

    @Column(name = "feed_size", nullable = false)
    private FeedSize feedSize;

    @Column(name = "feed_quantity_kg", nullable = false)
    private BigDecimal feedQuantityKg;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FeedEntryStatus status =
            FeedEntryStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}