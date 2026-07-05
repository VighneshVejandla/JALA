package com.jala.backend.feedschedule.service;

import com.jala.backend.feedschedule.dto.request.CreateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.request.UpdateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.response.FeedScheduleResponse;

import java.util.List;
import java.util.UUID;

public interface FeedScheduleService {

    List<FeedScheduleResponse> createSchedules(
            CreateFeedScheduleRequest request);

    List<FeedScheduleResponse> getSchedulesByCycle(
            UUID pondCycleId);

    FeedScheduleResponse updateSchedule(
            UUID id,
            UpdateFeedScheduleRequest request);

    void deactivateSchedule(UUID id);

    void activateSchedule(UUID id);
}