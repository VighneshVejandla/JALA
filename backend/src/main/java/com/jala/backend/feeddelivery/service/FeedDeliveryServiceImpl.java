package com.jala.backend.feeddelivery.service;

import com.jala.backend.common.constants.FeedConstants;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.util.DateTimeUtil;
import com.jala.backend.common.util.PageRequestUtil;
import com.jala.backend.feeddelivery.dto.request.AddSiteDeliveryRequest;
import com.jala.backend.feeddelivery.dto.request.CreateFeedDeliveryRequest;
import com.jala.backend.feeddelivery.dto.response.FeedDeliveryResponse;
import com.jala.backend.feeddelivery.dto.response.SiteDeliveryResponse;
import com.jala.backend.feeddelivery.entity.FeedDelivery;
import com.jala.backend.feeddelivery.entity.SiteDelivery;
import com.jala.backend.feeddelivery.enums.FeedDeliveryStatus;
import com.jala.backend.feeddelivery.mapper.FeedDeliveryMapper;
import com.jala.backend.feeddelivery.mapper.SiteDeliveryMapper;
import com.jala.backend.feeddelivery.repository.FeedDeliveryRepository;
import com.jala.backend.feeddelivery.repository.SiteDeliveryRepository;
import com.jala.backend.feedinventory.service.FeedInventoryService;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import com.jala.backend.user.entity.User;
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
public class FeedDeliveryServiceImpl implements FeedDeliveryService {

    private final FeedDeliveryRepository repository;

    private final FeedDeliveryMapper mapper;

    private final CurrentUserService currentUserService;

    private final SiteAccessService siteAccessService;

    private final SiteDeliveryRepository siteDeliveryRepository;

    private final SiteDeliveryMapper siteDeliveryMapper;

    private final SiteRepository siteRepository;

    private final FeedInventoryService feedInventoryService;

    @Override
    @Transactional
    public FeedDeliveryResponse createDelivery(
            CreateFeedDeliveryRequest request) {

        User user = currentUserService.getCurrentUser();

        FeedDelivery delivery = mapper.toEntity(request);

        delivery.setDeliveredBy(user);

        delivery.setDeliveredAt(DateTimeUtil.now());

        delivery.setStatus(FeedDeliveryStatus.ACTIVE);

        FeedDelivery saved = repository.save(delivery);

        log.info("Feed delivery {} created successfully.",
                saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedDeliveryResponse getDelivery(
            UUID id) {

        return mapper.toResponse(getDeliveryOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedDeliveryResponse> getAllDeliveries(
            Integer page,
            Integer size) {

        return repository
                .findByStatusOrderByDeliveredAtDesc(
                        FeedDeliveryStatus.ACTIVE,
                        PageRequestUtil.of(page, size))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SiteDeliveryResponse addSiteDelivery(
            UUID feedDeliveryId,
            AddSiteDeliveryRequest request) {

        siteAccessService.checkSiteAccess(request.getSiteId());

        FeedDelivery delivery =
                getDeliveryOrThrow(feedDeliveryId);

        Site site =
                getSiteOrThrow(request.getSiteId());

        SiteDelivery siteDelivery =
                siteDeliveryMapper.toEntity(request);

        siteDelivery.setFeedDelivery(delivery);

        siteDelivery.setSite(site);

        siteDelivery.setBagWeightKg(
                FeedConstants.DEFAULT_BAG_WEIGHT_KG);

        BigDecimal totalKg =
                FeedConstants.DEFAULT_BAG_WEIGHT_KG.multiply(
                        BigDecimal.valueOf(
                                request.getNumberOfBags()));

        siteDelivery.setTotalKg(totalKg);

        siteDelivery.setStatus(
                FeedDeliveryStatus.ACTIVE);

        SiteDelivery saved =
                siteDeliveryRepository.save(siteDelivery);

        // Inventory integration
        feedInventoryService.increaseInventory(
                site.getId(),
                totalKg);

        log.info(
                "Site delivery created for site {}",
                site.getSiteCode());

        return siteDeliveryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteDeliveryResponse> getSiteDeliveries(
            UUID feedDeliveryId) {

        return siteDeliveryRepository
                .findByFeedDeliveryIdAndStatusOrderById(
                        feedDeliveryId,
                        FeedDeliveryStatus.ACTIVE)
                .stream()
                .map(siteDeliveryMapper::toResponse)
                .toList();
    }

    private FeedDelivery getDeliveryOrThrow(
            UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feed delivery not found."));
    }

    private Site getSiteOrThrow(UUID id) {

        return siteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Site not found."));
    }
}
