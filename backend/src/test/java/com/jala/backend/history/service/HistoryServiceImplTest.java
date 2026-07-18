package com.jala.backend.history.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.history.dto.response.FeedHistoryResponse;
import com.jala.backend.history.dto.response.PondCycleHistoryResponse;
import com.jala.backend.history.mapper.HistoryMapper;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.medicinephoto.repository.MedicinePhotoRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private HistoryMapper mapper;

    @Mock
    private MedicinePhotoRepository medicinePhotoRepository;

    @Mock
    private PondRepository pondRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private HistoryServiceImpl service;

    private Pond pond;
    private PondCycle cycle;

    @BeforeEach
    void setUp() {
        pond = Pond.builder()
                .id(UUID.randomUUID()).pondCode("P-001").pondName("Pond 1").build();
        cycle = PondCycle.builder()
                .id(UUID.randomUUID()).pond(pond).cycleNumber(1)
                .status(PondCycleStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("getPondCycleHistory folds per-child aggregate counts onto each cycle")
    void getPondCycleHistory_success() {
        when(feedEntryRepository.countActiveByCycleForPond(pond.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{cycle.getId(), 6L}));
        when(medicineRepository.countActiveByCycleForPond(pond.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{cycle.getId(), 2L}));
        when(harvestRepository.countActiveByCycleForPond(pond.getId()))
                .thenReturn(List.<Object[]>of(new Object[]{cycle.getId(), 1L}));
        when(pondCycleRepository.findByPondIdOrderByCycleNumberDesc(pond.getId()))
                .thenReturn(List.of(cycle));
        when(mapper.toResponse(cycle)).thenReturn(PondCycleHistoryResponse.builder().build());

        List<PondCycleHistoryResponse> result =
                service.getPondCycleHistory(pond.getId());

        assertThat(result).hasSize(1);
        PondCycleHistoryResponse r = result.get(0);
        assertThat(r.getTotalFeedEntries()).isEqualTo(6);
        assertThat(r.getTotalMedicineEntries()).isEqualTo(2);
        assertThat(r.getTotalHarvests()).isEqualTo(1);
        assertThat(r.getCurrentCycle()).isTrue();
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getFeedHistory checks access and maps the page")
    void getFeedHistory_success() {
        FeedEntry entry = FeedEntry.builder()
                .id(UUID.randomUUID()).pondCycle(cycle).build();
        when(feedEntryRepository
                .findByPondCyclePondIdOrderByFeedDateDescIdDesc(
                        any(), any()))
                .thenReturn(List.of(entry));
        when(mapper.toFeedHistoryResponse(entry))
                .thenReturn(FeedHistoryResponse.builder().build());

        assertThat(service.getFeedHistory(pond.getId(), null, null)).hasSize(1);
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getTimeline rejects an unknown pond")
    void getTimeline_notFound() {
        when(pondRepository.findById(pond.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTimeline(pond.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found.");

        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getHarvestHistory checks access and maps the page")
    void getHarvestHistory_success() {
        when(harvestRepository
                .findByPondCyclePondIdOrderByHarvestDateDescUploadedAtDesc(any(), any()))
                .thenReturn(List.of(
                        com.jala.backend.harvest.entity.Harvest.builder()
                                .id(UUID.randomUUID()).pondCycle(cycle).build()));
        when(mapper.toHarvestResponse(any()))
                .thenReturn(com.jala.backend.history.dto.response.HarvestHistoryResponse
                        .builder().build());

        assertThat(service.getHarvestHistory(pond.getId(), null, null)).hasSize(1);
        verify(siteAccessService).checkPondAccess(pond.getId());
    }

    @Test
    @DisplayName("getMedicineHistory attaches batched photos to each entry")
    void getMedicineHistory_success() {
        com.jala.backend.medicine.entity.MedicineEntry me =
                com.jala.backend.medicine.entity.MedicineEntry.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).build();
        var response =
                com.jala.backend.history.dto.response.MedicineHistoryResponse
                        .builder().build();

        when(medicineRepository
                .findByPondCyclePondIdOrderByCreatedAtDesc(any(), any()))
                .thenReturn(List.of(me));
        when(medicinePhotoRepository
                .findByMedicineEntryIdInOrderByUploadedAt(any()))
                .thenReturn(List.of());
        when(mapper.toMedicineHistoryResponse(me)).thenReturn(response);

        var result = service.getMedicineHistory(pond.getId(), null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhotos()).isEmpty();
    }

    @Test
    @DisplayName("getTimeline merges cycle, feed, medicine and harvest events")
    void getTimeline_success() {
        cycle.setCreatedAt(java.time.LocalDateTime.now());
        cycle.setStockingDate(java.time.LocalDate.now());
        cycle.setShrimpCount(10000);
        cycle.setSpecies(com.jala.backend.pondcycle.enums.ShrimpSpecies.VANNAMEI);

        when(pondRepository.findById(pond.getId())).thenReturn(Optional.of(pond));
        when(pondCycleRepository.findByPondIdOrderByCycleNumberDesc(pond.getId()))
                .thenReturn(List.of(cycle));
        when(feedEntryRepository.findByPondCyclePondId(pond.getId()))
                .thenReturn(List.of());
        when(medicineRepository.findByPondCyclePondId(pond.getId()))
                .thenReturn(List.of());
        when(harvestRepository.findByPondCyclePondId(pond.getId()))
                .thenReturn(List.of());

        var result = service.getTimeline(pond.getId());

        assertThat(result.getPondCode()).isEqualTo("P-001");
        // one CYCLE + one STOCKING event from the single cycle
        assertThat(result.getTimeline()).hasSize(2);
    }
}
