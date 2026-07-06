package com.jala.backend.pondcycle.entity;

import com.jala.backend.common.entity.BaseEntity;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pond_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PondCycle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pond_id", nullable = false)
    private Pond pond;

    @Enumerated(EnumType.STRING)
    @Column
    private ShrimpSpecies species;

    @Column(name = "stocking_date")
    private LocalDate stockingDate;

    @Column(name = "shrimp_count")
    private Integer shrimpCount;

    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PondCycleStatus status = PondCycleStatus.ACTIVE;
}