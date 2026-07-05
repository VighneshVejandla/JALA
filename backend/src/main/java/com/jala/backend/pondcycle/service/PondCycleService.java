package com.jala.backend.pondcycle.service;

import com.jala.backend.pondcycle.dto.request.CreatePondCycleRequest;
import com.jala.backend.pondcycle.dto.request.UpdatePondCycleRequest;
import com.jala.backend.pondcycle.dto.response.PondCycleResponse;

import java.util.List;
import java.util.UUID;

public interface PondCycleService {

    PondCycleResponse createCycle(CreatePondCycleRequest request);

    PondCycleResponse getActiveCycle(UUID pondId);

    List<PondCycleResponse> getCyclesByPond(UUID pondId);

    PondCycleResponse updateCycle(
            UUID id,
            UpdatePondCycleRequest request);

    void harvestCycle(UUID id);

    void undoHarvest(UUID id);
}