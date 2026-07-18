package com.jala.backend.feeddelivery.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feeddelivery.dto.request.AddSiteDeliveryRequest;
import com.jala.backend.feeddelivery.dto.request.CreateFeedDeliveryRequest;
import com.jala.backend.feeddelivery.dto.response.FeedDeliveryResponse;
import com.jala.backend.feeddelivery.dto.response.SiteDeliveryResponse;
import com.jala.backend.feeddelivery.entity.FeedDelivery;
import com.jala.backend.feeddelivery.entity.SiteDelivery;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedDeliveryServiceImplTest {

    @Mock
    private FeedDeliveryRepository repository;

    @Mock
    private FeedDeliveryMapper mapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SiteAccessService siteAccessService;

    @Mock
    private SiteDeliveryRepository siteDeliveryRepository;

    @Mock
    private SiteDeliveryMapper siteDeliveryMapper;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private FeedInventoryService feedInventoryService;

    @InjectMocks
    private FeedDeliveryServiceImpl service;

    private FeedDelivery delivery;
    private Site site;

    @BeforeEach
    void setUp() {
        delivery = FeedDelivery.builder().id(UUID.randomUUID()).build();
        site = Site.builder()
                .id(UUID.randomUUID())
                .siteCode("S-001")
                .build();
    }

    @Test
    @DisplayName("createDelivery stamps the current user and saves")
    void createDelivery_success() {
        CreateFeedDeliveryRequest request = new CreateFeedDeliveryRequest();
        User user = User.builder().id(UUID.randomUUID()).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(mapper.toEntity(request)).thenReturn(delivery);
        when(repository.save(delivery)).thenReturn(delivery);
        when(mapper.toResponse(delivery))
                .thenReturn(FeedDeliveryResponse.builder().build());

        service.createDelivery(request);

        verify(repository).save(delivery);
    }

    @Test
    @DisplayName("getDelivery rejects an unknown id")
    void getDelivery_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDelivery(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Feed delivery not found.");
    }

    @Test
    @DisplayName("addSiteDelivery computes total kg and credits inventory")
    void addSiteDelivery_success() {
        AddSiteDeliveryRequest request = new AddSiteDeliveryRequest();
        request.setSiteId(site.getId());
        request.setNumberOfBags(4);

        SiteDelivery siteDelivery = new SiteDelivery();

        when(repository.findById(delivery.getId()))
                .thenReturn(Optional.of(delivery));
        when(siteRepository.findById(site.getId()))
                .thenReturn(Optional.of(site));
        when(siteDeliveryMapper.toEntity(request)).thenReturn(siteDelivery);
        when(siteDeliveryRepository.save(siteDelivery)).thenReturn(siteDelivery);
        when(siteDeliveryMapper.toResponse(siteDelivery))
                .thenReturn(SiteDeliveryResponse.builder().build());

        service.addSiteDelivery(delivery.getId(), request);

        // 4 bags * 25kg default bag weight = 100kg
        verify(siteAccessService).checkSiteAccess(site.getId());
        verify(feedInventoryService).increaseInventory(
                eq(site.getId()), eq(new BigDecimal("100")));
    }

    @Test
    @DisplayName("addSiteDelivery rejects an unknown site")
    void addSiteDelivery_siteNotFound() {
        AddSiteDeliveryRequest request = new AddSiteDeliveryRequest();
        request.setSiteId(site.getId());
        request.setNumberOfBags(1);

        when(repository.findById(delivery.getId()))
                .thenReturn(Optional.of(delivery));
        when(siteRepository.findById(site.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.addSiteDelivery(delivery.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Site not found.");
    }
}
