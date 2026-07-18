package com.jala.backend.pondcycle.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.dto.request.CreatePondCycleRequest;
import com.jala.backend.pondcycle.dto.request.UpdatePondCycleRequest;
import com.jala.backend.pondcycle.dto.response.PondCycleResponse;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.enums.ShrimpSpecies;
import com.jala.backend.pondcycle.mapper.PondCycleMapper;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.site.entity.Site;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class PondCycleServiceImplTest {

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private PondRepository pondRepository;

    @Mock
    private PondCycleMapper pondCycleMapper;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private PondCycleServiceImpl service;

    private Site site;
    private Pond pond;
    private PondCycle cycle;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID())
                .siteCode("S-001")
                .siteName("Site One")
                .ownerName("Owner")
                .location("Location")
                .totalAcres(BigDecimal.TEN)
                .build();

        pond = Pond.builder()
                .id(UUID.randomUUID())
                .site(site)
                .pondCode("P-001")
                .pondName("Pond One")
                .pondAcres(BigDecimal.ONE)
                .isActive(true)
                .build();

        cycle = PondCycle.builder()
                .id(UUID.randomUUID())
                .pond(pond)
                .cycleNumber(1)
                .species(ShrimpSpecies.VANNAMEI)
                .stockingDate(LocalDate.of(2026, 1, 10))
                .shrimpCount(100_000)
                .status(PondCycleStatus.ACTIVE)
                .build();
    }

    private CreatePondCycleRequest createRequest() {
        CreatePondCycleRequest request = new CreatePondCycleRequest();
        request.setPondId(pond.getId());
        request.setSpecies(ShrimpSpecies.VANNAMEI);
        request.setStockingDate(LocalDate.of(2026, 1, 10));
        request.setShrimpCount(100_000);
        return request;
    }

    // ------------------------------------------------------------------
    // createCycle
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCycle saves ACTIVE cycle with cycle number 1 and returns response")
    void createCycle_success() {
        CreatePondCycleRequest request = createRequest();

        PondCycle mapped = PondCycle.builder()
                .species(request.getSpecies())
                .stockingDate(request.getStockingDate())
                .shrimpCount(request.getShrimpCount())
                .build();

        PondCycleResponse response = PondCycleResponse.builder()
                .id(UUID.randomUUID())
                .build();

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));
        when(pondCycleRepository.existsByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(false);
        when(pondCycleMapper.toEntity(request)).thenReturn(mapped);
        when(pondCycleRepository.save(any(PondCycle.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(pondCycleMapper.toResponse(mapped)).thenReturn(response);

        PondCycleResponse result = service.createCycle(request);

        assertThat(result).isSameAs(response);

        verify(siteAccessService).checkPondAccess(pond.getId());

        ArgumentCaptor<PondCycle> captor =
                ArgumentCaptor.forClass(PondCycle.class);
        verify(pondCycleRepository).save(captor.capture());

        PondCycle saved = captor.getValue();
        assertThat(saved.getPond()).isSameAs(pond);
        assertThat(saved.getStatus()).isEqualTo(PondCycleStatus.ACTIVE);
        assertThat(saved.getCycleNumber()).isEqualTo(1);
        assertThat(saved.getSpecies()).isEqualTo(ShrimpSpecies.VANNAMEI);
        assertThat(saved.getStockingDate())
                .isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(saved.getShrimpCount()).isEqualTo(100_000);
    }

    @Test
    @DisplayName("createCycle accepts null stocking fields from the mapper")
    void createCycle_nullableStockingFields() {
        CreatePondCycleRequest request = createRequest();

        PondCycle mapped = PondCycle.builder().build();

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));
        when(pondCycleRepository.existsByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(false);
        when(pondCycleMapper.toEntity(request)).thenReturn(mapped);
        when(pondCycleRepository.save(any(PondCycle.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(pondCycleMapper.toResponse(mapped))
                .thenReturn(PondCycleResponse.builder().build());

        service.createCycle(request);

        ArgumentCaptor<PondCycle> captor =
                ArgumentCaptor.forClass(PondCycle.class);
        verify(pondCycleRepository).save(captor.capture());

        PondCycle saved = captor.getValue();
        assertThat(saved.getSpecies()).isNull();
        assertThat(saved.getStockingDate()).isNull();
        assertThat(saved.getShrimpCount()).isNull();
        assertThat(saved.getStatus()).isEqualTo(PondCycleStatus.ACTIVE);
        assertThat(saved.getCycleNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("createCycle throws when pond does not exist")
    void createCycle_pondNotFound() {
        CreatePondCycleRequest request = createRequest();

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCycle(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found");

        verify(pondCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCycle rejects inactive pond")
    void createCycle_inactivePond() {
        CreatePondCycleRequest request = createRequest();
        pond.setIsActive(false);

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));

        assertThatThrownBy(() -> service.createCycle(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot start a cycle for an inactive pond.");

        verify(pondCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCycle rejects pond with an active cycle")
    void createCycle_duplicateActiveCycle() {
        CreatePondCycleRequest request = createRequest();

        when(pondRepository.findById(pond.getId()))
                .thenReturn(Optional.of(pond));
        when(pondCycleRepository.existsByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createCycle(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("This pond already has an active cycle.");

        verify(pondCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCycle propagates access denial before touching repositories")
    void createCycle_accessDenied() {
        CreatePondCycleRequest request = createRequest();

        doThrow(new AccessDeniedException("denied"))
                .when(siteAccessService)
                .checkPondAccess(pond.getId());

        assertThatThrownBy(() -> service.createCycle(request))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(pondRepository, pondCycleRepository);
    }

    // ------------------------------------------------------------------
    // getCyclesByPond
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getCyclesByPond maps repository results in order")
    void getCyclesByPond_success() {
        PondCycle second = PondCycle.builder()
                .id(UUID.randomUUID())
                .pond(pond)
                .cycleNumber(2)
                .status(PondCycleStatus.ACTIVE)
                .build();

        PondCycleResponse firstResponse =
                PondCycleResponse.builder().id(second.getId()).build();
        PondCycleResponse secondResponse =
                PondCycleResponse.builder().id(cycle.getId()).build();

        when(pondCycleRepository
                .findByPondIdOrderByCycleNumberDesc(pond.getId()))
                .thenReturn(List.of(second, cycle));
        when(pondCycleMapper.toResponse(second)).thenReturn(firstResponse);
        when(pondCycleMapper.toResponse(cycle)).thenReturn(secondResponse);

        List<PondCycleResponse> result =
                service.getCyclesByPond(pond.getId());

        assertThat(result).containsExactly(firstResponse, secondResponse);
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    // ------------------------------------------------------------------
    // getActiveCycle
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getActiveCycle returns the active cycle")
    void getActiveCycle_found() {
        PondCycleResponse response =
                PondCycleResponse.builder().id(cycle.getId()).build();

        when(pondCycleRepository.findByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(Optional.of(cycle));
        when(pondCycleMapper.toResponse(cycle)).thenReturn(response);

        PondCycleResponse result = service.getActiveCycle(pond.getId());

        assertThat(result).isSameAs(response);
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getActiveCycle throws when no active cycle exists")
    void getActiveCycle_absent() {
        UUID pondId = pond.getId();

        when(pondCycleRepository.findByPondIdAndStatus(
                pondId, PondCycleStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveCycle(pondId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No active cycle found");
    }

    // ------------------------------------------------------------------
    // updateCycle
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateCycle throws when cycle does not exist")
    void updateCycle_notFound() {
        UUID id = UUID.randomUUID();

        when(pondCycleRepository.findById(id))
                .thenReturn(Optional.empty());

        UpdatePondCycleRequest request = new UpdatePondCycleRequest();

        assertThatThrownBy(() -> service.updateCycle(id, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond cycle not found");
    }

    @Test
    @DisplayName("updateCycle rejects harvested cycles")
    void updateCycle_harvestedGuard() {
        cycle.setStatus(PondCycleStatus.HARVESTED);

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        UUID id = cycle.getId();
        UpdatePondCycleRequest request = new UpdatePondCycleRequest();

        assertThatThrownBy(() -> service.updateCycle(id, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Harvested cycles cannot be updated.");

        verify(siteAccessService).checkPondCycleAccess(id);
        verify(pondCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCycle applies all provided fields")
    void updateCycle_allFields() {
        UpdatePondCycleRequest request = new UpdatePondCycleRequest();
        request.setSpecies(ShrimpSpecies.TIGER);
        request.setStockingDate(LocalDate.of(2026, 2, 1));
        request.setShrimpCount(55_000);

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(pondCycleRepository.save(cycle)).thenReturn(cycle);
        when(pondCycleMapper.toResponse(cycle))
                .thenReturn(PondCycleResponse.builder().build());

        service.updateCycle(cycle.getId(), request);

        assertThat(cycle.getSpecies()).isEqualTo(ShrimpSpecies.TIGER);
        assertThat(cycle.getStockingDate())
                .isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(cycle.getShrimpCount()).isEqualTo(55_000);
        verify(pondCycleRepository).save(cycle);
    }

    @Test
    @DisplayName("updateCycle leaves fields unchanged when request is empty")
    void updateCycle_nullFieldsIgnored() {
        UpdatePondCycleRequest request = new UpdatePondCycleRequest();

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(pondCycleRepository.save(cycle)).thenReturn(cycle);
        when(pondCycleMapper.toResponse(cycle))
                .thenReturn(PondCycleResponse.builder().build());

        service.updateCycle(cycle.getId(), request);

        assertThat(cycle.getSpecies()).isEqualTo(ShrimpSpecies.VANNAMEI);
        assertThat(cycle.getStockingDate())
                .isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(cycle.getShrimpCount()).isEqualTo(100_000);
        verify(pondCycleRepository).save(cycle);
    }

    // ------------------------------------------------------------------
    // harvestCycle
    // ------------------------------------------------------------------

    @Test
    @DisplayName("harvestCycle marks the cycle HARVESTED")
    void harvestCycle_success() {
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        service.harvestCycle(cycle.getId());

        assertThat(cycle.getStatus()).isEqualTo(PondCycleStatus.HARVESTED);
        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        verify(pondCycleRepository).save(cycle);
    }

    @Test
    @DisplayName("harvestCycle rejects an already harvested cycle")
    void harvestCycle_alreadyHarvested() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        UUID id = cycle.getId();

        when(pondCycleRepository.findById(id))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.harvestCycle(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cycle already harvested.");

        verify(pondCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("harvestCycle throws when cycle does not exist")
    void harvestCycle_notFound() {
        UUID id = UUID.randomUUID();

        when(pondCycleRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.harvestCycle(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond cycle not found");
    }

    // ------------------------------------------------------------------
    // undoHarvest
    // ------------------------------------------------------------------

    @Test
    @DisplayName("undoHarvest restores a harvested cycle to ACTIVE")
    void undoHarvest_success() {
        cycle.setStatus(PondCycleStatus.HARVESTED);

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(pondCycleRepository.existsByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(false);

        service.undoHarvest(cycle.getId());

        assertThat(cycle.getStatus()).isEqualTo(PondCycleStatus.ACTIVE);
        verify(pondCycleRepository).save(cycle);
    }

    @Test
    @DisplayName("undoHarvest rejects a cycle that is not harvested")
    void undoHarvest_notHarvested() {
        UUID id = cycle.getId();

        when(pondCycleRepository.findById(id))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.undoHarvest(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only harvested cycles can be restored.");

        verify(pondCycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("undoHarvest rejects when another active cycle exists")
    void undoHarvest_anotherActiveCycleExists() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        UUID id = cycle.getId();

        when(pondCycleRepository.findById(id))
                .thenReturn(Optional.of(cycle));
        when(pondCycleRepository.existsByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.undoHarvest(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(
                        "Cannot undo harvest because another active cycle already exists.");

        verify(pondCycleRepository, never()).save(any());
    }
}
