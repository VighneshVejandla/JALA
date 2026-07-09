package com.jala.backend.history.service;

import com.jala.backend.history.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface HistoryService {

    List<PondCycleHistoryResponse> getPondCycleHistory(
            UUID pondId);

    List<HarvestHistoryResponse> getHarvestHistory(
            UUID pondId);

    List<FeedHistoryResponse> getFeedHistory(
            UUID pondId);

    List<MedicineHistoryResponse> getMedicineHistory(
            UUID pondId);

    PondTimelineResponse getTimeline(
            UUID pondId);

}