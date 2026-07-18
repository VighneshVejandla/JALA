package com.jala.backend.harvest.service;

import com.jala.backend.harvest.dto.request.CancelHarvestRequest;
import com.jala.backend.harvest.dto.request.CreateHarvestRequest;
import com.jala.backend.harvest.dto.response.HarvestResponse;

import java.util.List;
import java.util.UUID;

public interface HarvestService {

    HarvestResponse createHarvest(
            CreateHarvestRequest request);

    List<HarvestResponse> getHarvests(
            UUID pondCycleId,
            Integer page,
            Integer size);

    HarvestResponse cancelHarvest(
            UUID harvestId,
            CancelHarvestRequest request);
}