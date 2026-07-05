package com.jala.backend.pond.service;

import com.jala.backend.pond.dto.request.CreatePondRequest;
import com.jala.backend.pond.dto.request.UpdatePondRequest;
import com.jala.backend.pond.dto.response.PondResponse;

import java.util.List;
import java.util.UUID;

public interface PondService {

    PondResponse createPond(CreatePondRequest request);

    List<PondResponse> getAllPonds();

    List<PondResponse> getPondsBySite(UUID siteId);

    PondResponse getPondById(UUID id);

    PondResponse patchPond(UUID id, UpdatePondRequest request);

    void activatePond(UUID id);

    void deactivatePond(UUID id);
}