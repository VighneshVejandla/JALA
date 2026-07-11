package com.jala.backend.search.service;

import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.notification.repository.NotificationRepository;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.search.dto.response.GlobalSearchResponse;
import com.jala.backend.search.dto.response.SearchResultResponse;
import com.jala.backend.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl
        implements SearchService {

    private final SiteRepository siteRepository;

    private final PondRepository pondRepository;

    private final FeedEntryRepository feedEntryRepository;

    private final MedicineRepository medicineRepository;

    private final HarvestRepository harvestRepository;

    private final NotificationRepository notificationRepository;

    @Override
    public GlobalSearchResponse search(
            String keyword) {

        return GlobalSearchResponse.builder()

                .sites(searchSites(keyword))

                .ponds(searchPonds(keyword))

                .feedEntries(searchFeed(keyword))

                .medicineEntries(searchMedicine(keyword))

                .harvests(searchHarvest(keyword))

                .notifications(searchNotifications(keyword))

                .build();
    }

    private List<SearchResultResponse> searchSites(
            String keyword) {

        return siteRepository.search(keyword)
                .stream()
                .map(site ->
                        SearchResultResponse.builder()
                                .id(site.getId())
                                .type("SITE")
                                .title(site.getSiteCode())
                                .subtitle(site.getSiteName())
                                .build())
                .toList();
    }

    private List<SearchResultResponse> searchPonds(
            String keyword) {

        return pondRepository.search(keyword)
                .stream()
                .map(pond ->
                        SearchResultResponse.builder()
                                .id(pond.getId())
                                .type("POND")
                                .title(pond.getPondCode())
                                .subtitle(
                                        pond.getSite().getSiteCode()
                                                + " - "
                                                + pond.getPondName())
                                .build())
                .toList();
    }

    private List<SearchResultResponse> searchFeed(
            String keyword) {

        return feedEntryRepository.search(keyword)
                .stream()
                .map(feed ->
                        SearchResultResponse.builder()
                                .id(feed.getId())
                                .type("FEED")
                                .title(feed.getPondCycle()
                                        .getPond()
                                        .getPondCode())
                                .subtitle(feed.getRemarks())
                                .build())
                .toList();
    }

    private List<SearchResultResponse> searchMedicine(
            String keyword) {

        return medicineRepository.search(keyword)
                .stream()
                .map(medicine ->
                        SearchResultResponse.builder()
                                .id(medicine.getId())
                                .type("MEDICINE")
                                .title(
                                        medicine.getPondCycle()
                                                .getPond()
                                                .getPondCode())
                                .subtitle(medicine.getRemarks())
                                .build())
                .toList();
    }

    private List<SearchResultResponse> searchHarvest(
            String keyword) {

        return harvestRepository.search(keyword)
                .stream()
                .map(harvest ->
                        SearchResultResponse.builder()
                                .id(harvest.getId())
                                .type("HARVEST")
                                .title(
                                        harvest.getPondCycle()
                                                .getPond()
                                                .getPondCode())
                                .subtitle(harvest.getBuyerName())
                                .build())
                .toList();
    }

    private List<SearchResultResponse> searchNotifications(
            String keyword) {

        return notificationRepository.search(keyword)
                .stream()
                .map(notification ->
                        SearchResultResponse.builder()
                                .id(notification.getId())
                                .type("NOTIFICATION")
                                .title(notification.getTitle())
                                .subtitle(notification.getMessage())
                                .build())
                .toList();
    }

}