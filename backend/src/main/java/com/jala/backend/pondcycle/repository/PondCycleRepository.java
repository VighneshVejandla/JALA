package com.jala.backend.pondcycle.repository;

import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PondCycleRepository
        extends JpaRepository<PondCycle, UUID> {

    Optional<PondCycle> findByPondIdAndStatus(
            UUID pondId,
            PondCycleStatus status
    );

    List<PondCycle> findByPondIdOrderByCycleNumberDesc(
            UUID pondId);

    boolean existsByPondIdAndStatus(
            UUID pondId,
            PondCycleStatus status
    );

    long countByPondSiteIdAndStatus(
            UUID siteId,
            PondCycleStatus status);

}