package com.jala.backend.history.service;

import com.jala.backend.history.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface HistoryService {

    List<PondCycleHistoryResponse> getPondCycleHistory(
            UUID pondId);

    List<HarvestHistoryResponse> getHarvestHistory(
            UUID pondId,
            Integer page,
            Integer size);

    List<FeedHistoryResponse> getFeedHistory(
            UUID pondId,
            Integer page,
            Integer size);

    List<MedicineHistoryResponse> getMedicineHistory(
            UUID pondId,
            Integer page,
            Integer size);

    PondTimelineResponse getTimeline(
            UUID pondId);

}
