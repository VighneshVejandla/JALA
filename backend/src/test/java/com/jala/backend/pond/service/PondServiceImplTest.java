package com.jala.backend.pond.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.pond.dto.request.CreatePondRequest;
import com.jala.backend.pond.dto.request.UpdatePondRequest;
import com.jala.backend.pond.dto.response.PondResponse;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.mapper.PondMapper;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.site.entity.Site;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PondServiceImplTest {

    @Mock
    private PondRepository pondRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PondMapper pondMapper;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private PondServiceImpl pondService;

    private Site site;
    private Pond pond;

    @BeforeEach
    void setUp() {

        site = Site.builder()
                .id(UUID.randomUUID())
                .siteCode("S-001")
                .siteName("Site 1")
                .ownerName("Owner")
                .location("Location")
                .totalAcres(new BigDecimal("10.00"))
                .isActive(true)
                .build();

        pond = Pond.builder()
                .id(UUID.randomUUID())
                .site(site)
                .pondCode("P-001")
                .pondName("Pond 1")
                .pondAcres(new BigDecimal("1.50"))
                .isActive(true)
                .build();
    }

    private static PondResponse response(Pond pond) {
        return PondResponse.builder()
                .id(pond.getId())
                .pondCode(pond.getPondCode())
                .build();
    }

    @Nested
    class CreatePond {

        private CreatePondRequest request() {
            CreatePondRequest request = new CreatePondRequest();
            request.setSiteId(site.getId());
            request.setPondCode("P-002");
            request.setPondName("Pond 2");
            request.setPondAcres(new BigDecimal("2.00"));
            return request;
        }

        @Test
        @DisplayName("happy path attaches the site and saves the pond")
        void createPond_success() {

            CreatePondRequest request = request();

            when(siteRepository.findById(site.getId()))
                    .thenReturn(Optional.of(site));
            when(pondRepository.existsBySiteIdAndPondCode(
                    site.getId(), "P-002")).thenReturn(false);

            Pond mapped = Pond.builder()
                    .id(UUID.randomUUID())
                    .pondCode("P-002")
                    .pondName("Pond 2")
                    .pondAcres(new BigDecimal("2.00"))
                    .build();

            when(pondMapper.toEntity(request)).thenReturn(mapped);
            when(pondRepository.save(mapped)).thenReturn(mapped);

            PondResponse expected = response(mapped);
            when(pondMapper.toResponse(mapped)).thenReturn(expected);

            PondResponse actual = pondService.createPond(request);

            assertThat(actual).isSameAs(expected);
            assertThat(mapped.getSite()).isSameAs(site);
            verify(siteAccessService).checkSiteAccess(site.getId());
        }

        @Test
        @DisplayName("unknown site is rejected")
        void createPond_siteNotFound() {

            CreatePondRequest request = request();

            when(siteRepository.findById(site.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> pondService.createPond(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Site not found");

            verify(pondRepository, never()).save(any());
        }

        @Test
        @DisplayName("duplicate pond code for the site is rejected")
        void createPond_duplicatePondCode() {

            CreatePondRequest request = request();

            when(siteRepository.findById(site.getId()))
                    .thenReturn(Optional.of(site));
            when(pondRepository.existsBySiteIdAndPondCode(
                    site.getId(), "P-002")).thenReturn(true);

            assertThatThrownBy(() -> pondService.createPond(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Pond code already exists for this site");

            verify(pondRepository, never()).save(any());
        }

        @Test
        @DisplayName("site access denial propagates before any lookup")
        void createPond_accessDenied() {

            CreatePondRequest request = request();

            doThrow(new AccessDeniedException(
                    "You do not have access to this site"))
                    .when(siteAccessService).checkSiteAccess(site.getId());

            assertThatThrownBy(() -> pondService.createPond(request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You do not have access to this site");

            verifyNoInteractions(siteRepository, pondRepository, pondMapper);
        }
    }

    @Nested
    class GetAllPonds {

        @Test
        @DisplayName("unrestricted user (null scope) reads all ponds")
        void getAllPonds_unrestricted() {

            when(siteAccessService.accessibleSiteIds()).thenReturn(null);
            when(pondRepository.findAll()).thenReturn(List.of(pond));
            when(pondMapper.toResponse(pond)).thenReturn(response(pond));

            List<PondResponse> responses = pondService.getAllPonds();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo(pond.getId());
            verify(pondRepository, never())
                    .findBySiteIdInOrderByPondCode(any());
        }

        @Test
        @DisplayName("user with no assigned sites gets an empty list")
        void getAllPonds_noAccessibleSites() {

            when(siteAccessService.accessibleSiteIds())
                    .thenReturn(List.of());

            assertThat(pondService.getAllPonds()).isEmpty();

            verifyNoInteractions(pondRepository);
        }

        @Test
        @DisplayName("restricted user reads only ponds of assigned sites")
        void getAllPonds_restricted() {

            List<UUID> siteIds = List.of(site.getId());

            when(siteAccessService.accessibleSiteIds()).thenReturn(siteIds);
            when(pondRepository.findBySiteIdInOrderByPondCode(siteIds))
                    .thenReturn(List.of(pond));
            when(pondMapper.toResponse(pond)).thenReturn(response(pond));

            List<PondResponse> responses = pondService.getAllPonds();

            assertThat(responses).hasSize(1);
            verify(pondRepository, never()).findAll();
        }
    }

    @Test
    @DisplayName("getPondsBySite checks access and maps the result")
    void getPondsBySite_success() {

        when(pondRepository.findBySiteIdOrderByPondCode(site.getId()))
                .thenReturn(List.of(pond));
        when(pondMapper.toResponse(pond)).thenReturn(response(pond));

        List<PondResponse> responses =
                pondService.getPondsBySite(site.getId());

        assertThat(responses).hasSize(1);
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getPondById returns the mapped pond")
    void getPondById_found() {

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));

        PondResponse expected = response(pond);
        when(pondMapper.toResponse(pond)).thenReturn(expected);

        assertThat(pondService.getPondById(pond.getId()))
                .isSameAs(expected);
        verify(siteAccessService).checkSiteAccess(site.getId());
    }

    @Test
    @DisplayName("getPondById rejects unknown ids")
    void getPondById_notFound() {

        UUID id = UUID.randomUUID();
        when(pondRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pondService.getPondById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found");

        verifyNoInteractions(siteAccessService);
    }

    @Nested
    class PatchPond {

        @Test
        @DisplayName("new pond code colliding with another pond is rejected")
        void patchPond_duplicatePondCode() {

            UpdatePondRequest request = new UpdatePondRequest();
            request.setPondCode("P-999");

            when(pondRepository.findById(pond.getId()))
                    .thenReturn(Optional.of(pond));
            when(pondRepository.existsBySiteIdAndPondCode(
                    site.getId(), "P-999")).thenReturn(true);

            assertThatThrownBy(() ->
                    pondService.patchPond(pond.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Pond code already exists for this site");

            verify(pondRepository, never()).save(any());
        }

        @Test
        @DisplayName("available new pond code is applied")
        void patchPond_newPondCode_applied() {

            UpdatePondRequest request = new UpdatePondRequest();
            request.setPondCode("P-999");

            when(pondRepository.findById(pond.getId()))
                    .thenReturn(Optional.of(pond));
            when(pondRepository.existsBySiteIdAndPondCode(
                    site.getId(), "P-999")).thenReturn(false);
            when(pondRepository.save(pond)).thenReturn(pond);
            when(pondMapper.toResponse(pond)).thenReturn(response(pond));

            pondService.patchPond(pond.getId(), request);

            assertThat(pond.getPondCode()).isEqualTo("P-999");
        }

        @Test
        @DisplayName("unchanged pond code skips the duplicate check")
        void patchPond_samePondCode_skipsDuplicateCheck() {

            UpdatePondRequest request = new UpdatePondRequest();
            request.setPondCode("P-001");

            when(pondRepository.findById(pond.getId()))
                    .thenReturn(Optional.of(pond));
            when(pondRepository.save(pond)).thenReturn(pond);
            when(pondMapper.toResponse(pond)).thenReturn(response(pond));

            pondService.patchPond(pond.getId(), request);

            verify(pondRepository, never())
                    .existsBySiteIdAndPondCode(any(), any());
            assertThat(pond.getPondCode()).isEqualTo("P-001");
        }

        @Test
        @DisplayName("pond name, acres and active flag are applied when provided")
        void patchPond_otherFields_applied() {

            UpdatePondRequest request = new UpdatePondRequest();
            request.setPondName("Renamed");
            request.setPondAcres(new BigDecimal("3.25"));
            request.setIsActive(false);

            when(pondRepository.findById(pond.getId()))
                    .thenReturn(Optional.of(pond));
            when(pondRepository.save(pond)).thenReturn(pond);
            when(pondMapper.toResponse(pond)).thenReturn(response(pond));

            pondService.patchPond(pond.getId(), request);

            assertThat(pond.getPondName()).isEqualTo("Renamed");
            assertThat(pond.getPondAcres())
                    .isEqualByComparingTo("3.25");
            assertThat(pond.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("all-null request changes nothing but still saves")
        void patchPond_allNull_noChanges() {

            UpdatePondRequest request = new UpdatePondRequest();

            when(pondRepository.findById(pond.getId()))
                    .thenReturn(Optional.of(pond));
            when(pondRepository.save(pond)).thenReturn(pond);
            when(pondMapper.toResponse(pond)).thenReturn(response(pond));

            pondService.patchPond(pond.getId(), request);

            assertThat(pond.getPondCode()).isEqualTo("P-001");
            assertThat(pond.getPondName()).isEqualTo("Pond 1");
            assertThat(pond.getPondAcres()).isEqualByComparingTo("1.50");
            assertThat(pond.getIsActive()).isTrue();
            verify(pondRepository).save(pond);
        }

        @Test
        @DisplayName("unknown pond id is rejected")
        void patchPond_notFound() {

            UUID id = UUID.randomUUID();
            when(pondRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    pondService.patchPond(id, new UpdatePondRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Pond not found");
        }
    }

    @Test
    @DisplayName("activatePond sets the pond active")
    void activatePond_success() {

        pond.setIsActive(false);
        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));

        pondService.activatePond(pond.getId());

        assertThat(pond.getIsActive()).isTrue();
        verify(siteAccessService).checkSiteAccess(site.getId());
        verify(pondRepository).save(pond);
    }

    @Test
    @DisplayName("deactivatePond sets the pond inactive")
    void deactivatePond_success() {

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));

        pondService.deactivatePond(pond.getId());

        assertThat(pond.getIsActive()).isFalse();
        verify(siteAccessService).checkSiteAccess(site.getId());
        verify(pondRepository).save(pond);
    }

    @Test
    @DisplayName("activatePond rejects unknown ids")
    void activatePond_notFound() {

        UUID id = UUID.randomUUID();
        when(pondRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pondService.activatePond(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found");
    }

    @Test
    @DisplayName("deactivatePond rejects unknown ids")
    void deactivatePond_notFound() {

        UUID id = UUID.randomUUID();
        when(pondRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pondService.deactivatePond(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found");
    }
}
