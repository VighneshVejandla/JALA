package com.jala.backend.feedentry.service;

import com.jala.backend.feedentry.dto.request.CreateFeedEntryRequest;
import com.jala.backend.feedentry.dto.request.UpdateFeedEntryRequest;
import com.jala.backend.feedentry.dto.response.FeedEntryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FeedEntryService {

    FeedEntryResponse createFeedEntry(
            CreateFeedEntryRequest request);

    List<FeedEntryResponse> getFeedEntries(
            UUID pondCycleId,
            LocalDate date);

    FeedEntryResponse updateFeedEntry(
            UUID id,
            UpdateFeedEntryRequest request);

    void cancelFeedEntry(
            UUID id,
            String reason);

    void restoreFeedEntry(UUID id);
}