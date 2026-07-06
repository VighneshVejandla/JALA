package com.jala.backend.harvest.repository;

import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.enums.HarvestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HarvestRepository
        extends JpaRepository<Harvest, UUID> {

    List<Harvest> findByPondCycleIdOrderByHarvestDateDesc(
            UUID pondCycleId);

    List<Harvest> findByPondCycleIdAndStatusOrderByHarvestDateDesc(
            UUID pondCycleId,
            HarvestStatus status);

    long countByPondCycleId(
            UUID pondCycleId);

    Optional<Harvest> findByIdAndStatus(
            UUID id,
            HarvestStatus status);

    Optional<Harvest> findByPondCycleIdAndStatus(
            UUID pondCycleId,
            HarvestStatus status);
}