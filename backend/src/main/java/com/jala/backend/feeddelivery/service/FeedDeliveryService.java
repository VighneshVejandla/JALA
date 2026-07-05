package com.jala.backend.feeddelivery.service;

import com.jala.backend.feeddelivery.dto.request.AddSiteDeliveryRequest;
import com.jala.backend.feeddelivery.dto.request.CreateFeedDeliveryRequest;
import com.jala.backend.feeddelivery.dto.response.FeedDeliveryResponse;
import com.jala.backend.feeddelivery.dto.response.SiteDeliveryResponse;

import java.util.List;
import java.util.UUID;

public interface FeedDeliveryService {

    FeedDeliveryResponse createDelivery(
            CreateFeedDeliveryRequest request);

    FeedDeliveryResponse getDelivery(
            UUID id);

    List<FeedDeliveryResponse> getAllDeliveries();

    SiteDeliveryResponse addSiteDelivery(
            UUID feedDeliveryId,
            AddSiteDeliveryRequest request);

    List<SiteDeliveryResponse> getSiteDeliveries(
            UUID feedDeliveryId);
}