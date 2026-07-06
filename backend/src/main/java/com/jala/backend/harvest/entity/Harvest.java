package com.jala.backend.harvest.entity;

import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.harvest.enums.HarvestStatus;
import com.jala.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "harvests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Harvest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pond_cycle_id", nullable = false)
    private PondCycle pondCycle;

    @Column(nullable = false)
    private LocalDate harvestDate;

    /**
     * Always stored in Kilograms.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal harvestQuantityKg;

    /**
     * Public URL of the uploaded harvest bill.
     */
    @Column(nullable = false, length = 1000)
    private String billPhotoPath;

    // -----------------------------
    // Optional Commercial Details
    // -----------------------------

    @Column(length = 150)
    private String buyerName;

    @Column(precision = 12, scale = 2)
    private BigDecimal sellingPricePerKg;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 30)
    private String vehicleNumber;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HarvestStatus status = HarvestStatus.ACTIVE;

    // -----------------------------
    // Audit
    // -----------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancellationReason;
}