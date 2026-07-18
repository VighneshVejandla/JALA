package com.jala.backend.harvest.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.harvest.dto.request.CancelHarvestRequest;
import com.jala.backend.harvest.dto.request.CreateHarvestRequest;
import com.jala.backend.harvest.dto.response.HarvestResponse;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.enums.HarvestStatus;
import com.jala.backend.harvest.mapper.HarvestMapper;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.site.entity.Site;
import com.jala.backend.siteaccess.service.SiteAccessService;
import com.jala.backend.storage.service.StorageService;
import com.jala.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HarvestServiceImplTest {

    private static final byte[] PNG =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};

    @Mock
    private HarvestRepository repository;

    @Mock
    private HarvestMapper mapper;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SiteAccessService siteAccessService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private HarvestServiceImpl service;

    private Pond pond;
    private PondCycle cycle;
    private Harvest harvest;

    @BeforeEach
    void setUp() {
        Site site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").siteName("Site").build();
        pond = Pond.builder().id(UUID.randomUUID()).site(site).build();
        cycle = PondCycle.builder()
                .id(UUID.randomUUID()).pond(pond).cycleNumber(1)
                .status(PondCycleStatus.ACTIVE).build();
        harvest = Harvest.builder()
                .id(UUID.randomUUID()).pondCycle(cycle)
                .status(HarvestStatus.ACTIVE).build();
    }

    private CreateHarvestRequest createRequest() {
        CreateHarvestRequest r = new CreateHarvestRequest();
        r.setPondCycleId(cycle.getId());
        r.setHarvestDate(LocalDate.now());
        r.setHarvestQuantityKg(new BigDecimal("500.00"));
        r.setSellingPricePerKg(new BigDecimal("2.00"));
        r.setBillPhoto(new MockMultipartFile(
                "billPhoto", "bill.png", "image/png", PNG));
        return r;
    }

    @Test
    @DisplayName("createHarvest stores the bill, closes the cycle and opens the next")
    void createHarvest_success() {
        CreateHarvestRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(currentUserService.getCurrentUser())
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(repository.countByPondCycleId(cycle.getId())).thenReturn(0L);
        when(storageService.upload(any(), any(), any(), any()))
                .thenReturn("https://storage/harvest/bill.png");
        when(mapper.toEntity(request)).thenReturn(harvest);
        when(repository.save(harvest)).thenReturn(harvest);
        when(mapper.toResponse(harvest))
                .thenReturn(new HarvestResponse());

        service.createHarvest(request);

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        // total = 2.00 * 500 = 1000.00
        assertThat(harvest.getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(cycle.getStatus()).isEqualTo(PondCycleStatus.HARVESTED);
        // a fresh ACTIVE next cycle is persisted
        verify(pondCycleRepository).save(any(PondCycle.class));
    }

    @Test
    @DisplayName("createHarvest rejects an already-harvested cycle")
    void createHarvest_alreadyHarvested() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        CreateHarvestRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.createHarvest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been harvested");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cancelHarvest reverts the cycle and deletes the auto-created next cycle")
    void cancelHarvest_success() {
        CancelHarvestRequest request = new CancelHarvestRequest();
        request.setCancellationReason("entered by mistake");

        PondCycle nextCycle = PondCycle.builder()
                .id(UUID.randomUUID()).pond(pond)
                .status(PondCycleStatus.ACTIVE).build();

        when(repository.findByIdAndStatus(harvest.getId(), HarvestStatus.ACTIVE))
                .thenReturn(Optional.of(harvest));
        when(currentUserService.getCurrentUser())
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(pondCycleRepository.findByPondIdAndStatus(
                pond.getId(), PondCycleStatus.ACTIVE))
                .thenReturn(Optional.of(nextCycle));
        when(repository.save(harvest)).thenReturn(harvest);
        when(mapper.toResponse(harvest))
                .thenReturn(new HarvestResponse());

        service.cancelHarvest(harvest.getId(), request);

        verify(pondCycleRepository).delete(nextCycle);
        assertThat(cycle.getStatus()).isEqualTo(PondCycleStatus.ACTIVE);
        assertThat(harvest.getStatus()).isEqualTo(HarvestStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelHarvest rejects an unknown/active-missing harvest")
    void cancelHarvest_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndStatus(id, HarvestStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.cancelHarvest(id, new CancelHarvestRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Harvest not found.");
    }

    @Test
    @DisplayName("getHarvests enforces cycle access")
    void getHarvests_access() {
        when(repository.findByPondCycleIdOrderByHarvestDateDesc(
                any(), any())).thenReturn(java.util.List.of());

        service.getHarvests(cycle.getId(), null, null);

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
    }
}
