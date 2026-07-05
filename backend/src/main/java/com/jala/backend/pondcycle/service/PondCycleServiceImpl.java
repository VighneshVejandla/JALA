package com.jala.backend.pondcycle.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.dto.request.CreatePondCycleRequest;
import com.jala.backend.pondcycle.dto.request.UpdatePondCycleRequest;
import com.jala.backend.pondcycle.dto.response.PondCycleResponse;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.mapper.PondCycleMapper;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PondCycleServiceImpl implements PondCycleService {

    private final PondCycleRepository pondCycleRepository;
    private final PondRepository pondRepository;
    private final PondCycleMapper pondCycleMapper;

    @Override
    @Transactional
    public PondCycleResponse createCycle(CreatePondCycleRequest request) {

        log.info("Creating pond cycle for pond {}", request.getPondId());

        Pond pond = pondRepository.findById(request.getPondId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pond not found"));

        if (!pond.getIsActive()) {

            throw new BadRequestException(
                    "Cannot start a cycle for an inactive pond.");
        }

        if (pondCycleRepository.existsByPondIdAndStatus(
                pond.getId(),
                PondCycleStatus.ACTIVE)) {

            throw new BadRequestException(
                    "This pond already has an active cycle.");
        }

        PondCycle cycle = pondCycleMapper.toEntity(request);

        cycle.setPond(pond);

        cycle.setStatus(PondCycleStatus.ACTIVE);

        PondCycle savedCycle = pondCycleRepository.save(cycle);

        log.info("Pond cycle created successfully for pond {}",
                pond.getPondCode());

        return pondCycleMapper.toResponse(savedCycle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PondCycleResponse> getCyclesByPond(UUID pondId) {

        return pondCycleRepository
                .findByPondIdOrderByStockingDateDesc(pondId)
                .stream()
                .map(pondCycleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PondCycleResponse getActiveCycle(UUID pondId) {

        PondCycle cycle = pondCycleRepository
                .findByPondIdAndStatus(
                        pondId,
                        PondCycleStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No active cycle found"));

        return pondCycleMapper.toResponse(cycle);
    }

    @Override
    @Transactional
    public PondCycleResponse updateCycle(
            UUID id,
            UpdatePondCycleRequest request) {

        PondCycle cycle = getCycleOrThrow(id);

        if (cycle.getStatus() == PondCycleStatus.HARVESTED) {

            throw new BadRequestException(
                    "Harvested cycles cannot be updated.");
        }

        if (request.getSpecies() != null) {
            cycle.setSpecies(request.getSpecies());
        }

        if (request.getStockingDate() != null) {
            cycle.setStockingDate(request.getStockingDate());
        }

        if (request.getShrimpCount() != null) {
            cycle.setShrimpCount(request.getShrimpCount());
        }

        PondCycle updated = pondCycleRepository.save(cycle);

        log.info("Pond cycle updated {}", updated.getId());

        return pondCycleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void harvestCycle(UUID id) {

        PondCycle cycle = getCycleOrThrow(id);

        if (cycle.getStatus() == PondCycleStatus.HARVESTED) {

            throw new BadRequestException(
                    "Cycle already harvested.");
        }

        cycle.setStatus(PondCycleStatus.HARVESTED);

        pondCycleRepository.save(cycle);

        log.info("Cycle harvested {}", cycle.getId());
    }

    @Override
    @Transactional
    public void undoHarvest(UUID id) {

        PondCycle cycle = getCycleOrThrow(id);

        if (cycle.getStatus() != PondCycleStatus.HARVESTED) {
            throw new BadRequestException(
                    "Only harvested cycles can be restored.");
        }

        if (pondCycleRepository.existsByPondIdAndStatus(
                cycle.getPond().getId(),
                PondCycleStatus.ACTIVE)) {

            throw new BadRequestException(
                    "Cannot undo harvest because another active cycle already exists.");
        }

        cycle.setStatus(PondCycleStatus.ACTIVE);

        pondCycleRepository.save(cycle);

        log.info("Harvest undone for cycle {}", cycle.getId());
    }

    private PondCycle getCycleOrThrow(UUID id) {

        return pondCycleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pond cycle not found"));
    }
}
