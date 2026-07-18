package com.jala.backend.site.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedinventory.entity.FeedInventory;
import com.jala.backend.feedinventory.repository.FeedInventoryRepository;
import com.jala.backend.site.dto.request.CreateSiteRequest;
import com.jala.backend.site.dto.request.UpdateSiteRequest;
import com.jala.backend.site.dto.response.SiteResponse;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.mapper.SiteMapper;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SiteMapper siteMapper;

    @Mock
    private FeedInventoryRepository feedInventoryRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private SiteServiceImpl siteService;

    private Site site;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID())
                .siteCode("S-001")
                .siteName("Site 1")
                .ownerName("Owner")
                .location("Loc")
                .totalAcres(new BigDecimal("10.00"))
                .isActive(true)
                .build();
    }

    private static SiteResponse response(Site s) {
        return SiteResponse.builder().id(s.getId()).siteCode(s.getSiteCode()).build();
    }

    @Nested
    class CreateSite {

        private CreateSiteRequest request() {
            CreateSiteRequest r = new CreateSiteRequest();
            r.setSiteCode("S-002");
            r.setSiteName("Site 2");
            r.setOwnerName("Owner");
            r.setLocation("Loc");
            r.setTotalAcres(new BigDecimal("5.00"));
            return r;
        }

        @Test
        @DisplayName("creates the site and seeds a zeroed feed inventory")
        void createSite_success() {
            CreateSiteRequest request = request();
            when(siteRepository.existsBySiteCode("S-002")).thenReturn(false);
            when(siteMapper.toEntity(request)).thenReturn(site);
            when(siteRepository.save(site)).thenReturn(site);
            when(siteMapper.toResponse(site)).thenReturn(response(site));

            SiteResponse actual = siteService.createSite(request);

            assertThat(actual.getId()).isEqualTo(site.getId());
            verify(feedInventoryRepository).save(any(FeedInventory.class));
            verify(siteAccessService).checkSiteAccess(site.getId());
        }

        @Test
        @DisplayName("duplicate site code is rejected")
        void createSite_duplicate() {
            CreateSiteRequest request = request();
            when(siteRepository.existsBySiteCode("S-002")).thenReturn(true);

            assertThatThrownBy(() -> siteService.createSite(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Site Code already exists");

            verify(siteRepository, never()).save(any());
            verifyNoInteractions(feedInventoryRepository);
        }
    }

    @Nested
    class GetAllSites {

        @Test
        @DisplayName("unrestricted user reads all sites")
        void getAllSites_unrestricted() {
            when(siteAccessService.accessibleSiteIds()).thenReturn(null);
            when(siteRepository.findAll()).thenReturn(List.of(site));
            when(siteMapper.toResponse(site)).thenReturn(response(site));

            assertThat(siteService.getAllSites()).hasSize(1);
            verify(siteRepository, never()).findByIdIn(any());
        }

        @Test
        @DisplayName("user with no assigned sites gets an empty list")
        void getAllSites_none() {
            when(siteAccessService.accessibleSiteIds()).thenReturn(List.of());

            assertThat(siteService.getAllSites()).isEmpty();
            verify(siteRepository, never()).findAll();
            verify(siteRepository, never()).findByIdIn(any());
        }

        @Test
        @DisplayName("restricted user reads only assigned sites")
        void getAllSites_restricted() {
            List<UUID> ids = List.of(site.getId());
            when(siteAccessService.accessibleSiteIds()).thenReturn(ids);
            when(siteRepository.findByIdIn(ids)).thenReturn(List.of(site));
            when(siteMapper.toResponse(site)).thenReturn(response(site));

            assertThat(siteService.getAllSites()).hasSize(1);
            verify(siteRepository, never()).findAll();
        }
    }

    @Test
    @DisplayName("getSiteById checks access and maps")
    void getSiteById_found() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(siteMapper.toResponse(site)).thenReturn(response(site));

        assertThat(siteService.getSiteById(site.getId()).getId())
                .isEqualTo(site.getId());
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getSiteById rejects unknown ids")
    void getSiteById_notFound() {
        UUID id = UUID.randomUUID();
        when(siteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.getSiteById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Site not found");
    }

    @Test
    @DisplayName("patchSite applies only provided fields")
    void patchSite_partial() {
        UpdateSiteRequest request = new UpdateSiteRequest();
        request.setSiteName("Renamed");
        request.setIsActive(false);

        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(siteRepository.save(site)).thenReturn(site);
        when(siteMapper.toResponse(site)).thenReturn(response(site));

        siteService.patchSite(site.getId(), request);

        assertThat(site.getSiteName()).isEqualTo("Renamed");
        assertThat(site.getIsActive()).isFalse();
        assertThat(site.getOwnerName()).isEqualTo("Owner");
    }

    @Test
    @DisplayName("activateSite sets the site active")
    void activateSite_success() {
        site.setIsActive(false);
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

        siteService.activateSite(site.getId());

        assertThat(site.getIsActive()).isTrue();
        verify(siteRepository).save(site);
    }

    @Test
    @DisplayName("deactivateSite sets the site inactive")
    void deactivateSite_success() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

        siteService.deactivateSite(site.getId());

        assertThat(site.getIsActive()).isFalse();
        verify(siteRepository).save(site);
    }
}
