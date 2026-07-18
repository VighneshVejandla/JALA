package com.jala.backend.site.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.site.dto.request.CreateSiteRequest;
import com.jala.backend.site.dto.request.UpdateSiteRequest;
import com.jala.backend.site.dto.response.SiteResponse;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.mapper.SiteMapper;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final SiteMapper siteMapper;
    private final FeedInventoryRepository feedInventoryRepository;
    private final SiteAccessService siteAccessService;

    @Override
    @Transactional
    public SiteResponse createSite(CreateSiteRequest request) {

        log.info("Creating site {}", request.getSiteCode());

        if (siteRepository.existsBySiteCode(request.getSiteCode())) {
            throw new BadRequestException("Site Code already exists");
        }

        Site site = siteMapper.toEntity(request);

        Site savedSite = siteRepository.save(site);

        siteAccessService.checkSiteAccess(savedSite.getId());

        FeedInventory inventory = FeedInventory.builder()
                .site(savedSite)
                .totalReceivedKg(BigDecimal.ZERO)
                .totalConsumedKg(BigDecimal.ZERO)
                .availableKg(BigDecimal.ZERO)
                .updatedAt(DateTimeUtil.now())
                .build();

        feedInventoryRepository.save(inventory);

        log.info("Site {} created successfully", savedSite.getSiteCode());

        return siteMapper.toResponse(savedSite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteResponse> getAllSites() {

        List<UUID> accessibleSiteIds = siteAccessService.accessibleSiteIds();

        if (accessibleSiteIds != null && accessibleSiteIds.isEmpty()) {
            return List.of();
        }

        List<Site> sites = accessibleSiteIds == null
                ? siteRepository.findAll()
                : siteRepository.findByIdIn(accessibleSiteIds);

        return sites
                .stream()
                .map(siteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SiteResponse getSiteById(UUID id) {

        siteAccessService.checkSiteAccess(id);

        Site site = getSiteOrThrow(id);

        return siteMapper.toResponse(site);
    }

    @Override
    @Transactional
    public SiteResponse patchSite(UUID id, UpdateSiteRequest request) {

        siteAccessService.checkSiteAccess(id);

        Site site = getSiteOrThrow(id);

        if (request.getSiteName() != null &&
                !request.getSiteName().isBlank()) {

            site.setSiteName(request.getSiteName());
        }

        if (request.getOwnerName() != null &&
                !request.getOwnerName().isBlank()) {

            site.setOwnerName(request.getOwnerName());
        }

        if (request.getLocation() != null &&
                !request.getLocation().isBlank()) {

            site.setLocation(request.getLocation());
        }

        if (request.getTotalAcres() != null) {

            site.setTotalAcres(request.getTotalAcres());
        }

        if (request.getIsActive() != null) {

            site.setIsActive(request.getIsActive());
        }

        Site updatedSite = siteRepository.save(site);

        log.info("Site {} updated successfully",
                updatedSite.getSiteCode());

        return siteMapper.toResponse(updatedSite);
    }

    @Override
    @Transactional
    public void activateSite(UUID id) {

        siteAccessService.checkSiteAccess(id);

        Site site = getSiteOrThrow(id);

        site.setIsActive(true);

        siteRepository.save(site);

        log.info("Site {} activated", site.getSiteCode());
    }

    @Override
    @Transactional
    public void deactivateSite(UUID id) {

        siteAccessService.checkSiteAccess(id);

        Site site = getSiteOrThrow(id);

        site.setIsActive(false);

        siteRepository.save(site);

        log.info("Site {} deactivated", site.getSiteCode());
    }

    private Site getSiteOrThrow(UUID id) {

        return siteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Site not found"));
    }
}