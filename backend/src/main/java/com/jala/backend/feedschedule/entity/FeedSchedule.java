package com.jala.backend.feedschedule.entity;

import com.jala.backend.common.entity.BaseEntity;
import com.jala.backend.pondcycle.entity.PondCycle;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Entity
@Table(name = "feed_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pond_cycle_id", nullable = false)
    private PondCycle pondCycle;

    @Column(name = "session_number", nullable = false)
    private Integer sessionNumber;

    @Column(name = "feeding_time", nullable = false)
    private LocalTime feedingTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}