package com.jala.backend.search.service;

import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.notification.repository.NotificationRepository;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.search.dto.response.GlobalSearchResponse;
import com.jala.backend.search.dto.response.SearchResultResponse;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl
        implements SearchService {

    /** Caps each category so global search never scans unbounded rows. */
    private static final int MAX_RESULTS_PER_TYPE = 50;

    private static final Pageable RESULT_CAP =
            PageRequest.of(0, MAX_RESULTS_PER_TYPE);

    private final SiteRepository siteRepository;

    private final PondRepository pondRepository;

    private final FeedEntryRepository feedEntryRepository;

    private final MedicineRepository medicineRepository;

    private final HarvestRepository harvestRepository;

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    private final SiteAccessService siteAccessService;

    @Override
    public GlobalSearchResponse search(
            String keyword) {

        // null = unrestricted; otherwise results are limited to these sites.
        List<UUID> accessible = siteAccessService.accessibleSiteIds();

        Set<UUID> allowedSites =
                accessible == null ? null : Set.copyOf(accessible);

        return GlobalSearchResponse.builder()

                .users(searchUsers(keyword))

                .sites(searchSites(keyword, allowedSites))

                .ponds(searchPonds(keyword, allowedSites))

                .feedEntries(searchFeed(keyword, allowedSites))

                .medicineEntries(searchMedicine(keyword, allowedSites))

                .harvests(searchHarvest(keyword, allowedSites))

                .notifications(searchNotifications(keyword, allowedSites))

                .build();
    }

    private static boolean allowed(
            Set<UUID> allowedSites,
            UUID siteId) {

        return allowedSites == null || allowedSites.contains(siteId);
    }

    /** Only admins/managers can search the user directory. */
    private static boolean canSeeUsers() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_MANAGER"));
    }

    private List<SearchResultResponse> searchUsers(String keyword) {
        if (!canSeeUsers()) {
            return List.of();
        }
        return userRepository.search(keyword, RESULT_CAP)
                .stream()
                .map(user ->
                        SearchResultResponse.builder()
                                .id(user.getId())
                                .type("USER")
                                .title(user.getFullName())
                                .subtitle(user.getEmployeeCode())
                                .build())
                .toList();
    }

    private List<SearchResultResponse> searchSites(
            String keyword,
            Set<UUID> allowedSites) {

        return siteRepository.search(keyword, RESULT_CAP)
                .stream()
                .filter(site -> allowed(allowedSites, site.getId()))
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
            String keyword,
            Set<UUID> allowedSites) {

        return pondRepository.search(keyword, RESULT_CAP)
                .stream()
                .filter(pond ->
                        allowed(allowedSites, pond.getSite().getId()))
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
            String keyword,
            Set<UUID> allowedSites) {

        return feedEntryRepository.search(keyword, RESULT_CAP)
                .stream()
                .filter(feed -> allowed(
                        allowedSites,
                        feed.getPondCycle().getPond().getSite().getId()))
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
            String keyword,
            Set<UUID> allowedSites) {

        return medicineRepository.search(keyword, RESULT_CAP)
                .stream()
                .filter(medicine -> allowed(
                        allowedSites,
                        medicine.getPondCycle().getPond().getSite().getId()))
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
            String keyword,
            Set<UUID> allowedSites) {

        return harvestRepository.search(keyword, RESULT_CAP)
                .stream()
                .filter(harvest -> allowed(
                        allowedSites,
                        harvest.getPondCycle().getPond().getSite().getId()))
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
            String keyword,
            Set<UUID> allowedSites) {

        return notificationRepository.search(keyword, RESULT_CAP)
                .stream()
                .filter(notification ->
                        allowed(allowedSites, notification.getSiteId()))
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
