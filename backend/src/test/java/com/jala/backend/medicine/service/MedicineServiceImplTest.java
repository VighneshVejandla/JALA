package com.jala.backend.medicine.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.medicine.dto.request.CreateMedicineRequest;
import com.jala.backend.medicine.dto.request.UpdateMedicineRequest;
import com.jala.backend.medicine.dto.response.MedicineResponse;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.enums.MedicineStatus;
import com.jala.backend.medicine.mapper.MedicineMapper;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.site.entity.Site;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicineServiceImplTest {

    @Mock
    private MedicineRepository repository;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SiteAccessService siteAccessService;

    @Mock
    private MedicineMapper mapper;

    @InjectMocks
    private MedicineServiceImpl service;

    private PondCycle cycle;
    private MedicineEntry entry;
    private User user;

    @BeforeEach
    void setUp() {
        Pond pond = Pond.builder()
                .id(UUID.randomUUID())
                .site(Site.builder().id(UUID.randomUUID()).build())
                .build();

        cycle = PondCycle.builder()
                .id(UUID.randomUUID())
                .pond(pond)
                .status(PondCycleStatus.ACTIVE)
                .build();

        entry = MedicineEntry.builder()
                .id(UUID.randomUUID())
                .pondCycle(cycle)
                .status(MedicineStatus.ACTIVE)
                .build();

        user = User.builder().id(UUID.randomUUID()).build();
    }

    private CreateMedicineRequest createRequest() {
        CreateMedicineRequest r = new CreateMedicineRequest();
        r.setPondCycleId(cycle.getId());
        return r;
    }

    @Test
    @DisplayName("createMedicine on an active cycle persists the entry")
    void createMedicine_success() {
        CreateMedicineRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(mapper.toEntity(request)).thenReturn(entry);
        when(repository.save(entry)).thenReturn(entry);
        when(mapper.toResponse(entry))
                .thenReturn(MedicineResponse.builder().build());

        service.createMedicine(request);

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        assertThat(entry.getCreatedBy()).isSameAs(user);
        assertThat(entry.getStatus()).isEqualTo(MedicineStatus.ACTIVE);
    }

    @Test
    @DisplayName("createMedicine rejects an unknown cycle")
    void createMedicine_cycleNotFound() {
        CreateMedicineRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMedicine(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond cycle not found.");
    }

    @Test
    @DisplayName("createMedicine rejects a non-active cycle")
    void createMedicine_inactiveCycle() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        CreateMedicineRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.createMedicine(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("active pond cycle");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateMedicine applies provided fields")
    void updateMedicine_success() {
        UpdateMedicineRequest request = new UpdateMedicineRequest();
        request.setQuantity(new BigDecimal("5.00"));
        request.setRemarks("adjusted");

        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(repository.save(entry)).thenReturn(entry);
        when(mapper.toResponse(entry))
                .thenReturn(MedicineResponse.builder().build());

        service.updateMedicine(entry.getId(), request);

        assertThat(entry.getQuantity()).isEqualByComparingTo("5.00");
        assertThat(entry.getRemarks()).isEqualTo("adjusted");
        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
    }

    @Test
    @DisplayName("updateMedicine rejects a cancelled entry")
    void updateMedicine_cancelled() {
        entry.setStatus(MedicineStatus.CANCELLED);
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() ->
                service.updateMedicine(entry.getId(), new UpdateMedicineRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("cancelMedicine records who/why and flips status")
    void cancelMedicine_success() {
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(currentUserService.getCurrentUser()).thenReturn(user);

        service.cancelMedicine(entry.getId(), "wrong dose");

        assertThat(entry.getStatus()).isEqualTo(MedicineStatus.CANCELLED);
        assertThat(entry.getCancelledBy()).isSameAs(user);
        assertThat(entry.getCancellationReason()).isEqualTo("wrong dose");
    }

    @Test
    @DisplayName("cancelMedicine rejects an already-cancelled entry")
    void cancelMedicine_alreadyCancelled() {
        entry.setStatus(MedicineStatus.CANCELLED);
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() ->
                service.cancelMedicine(entry.getId(), "x"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("restoreMedicine reactivates a cancelled entry")
    void restoreMedicine_success() {
        entry.setStatus(MedicineStatus.CANCELLED);
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(currentUserService.getCurrentUser()).thenReturn(user);

        service.restoreMedicine(entry.getId());

        assertThat(entry.getStatus()).isEqualTo(MedicineStatus.ACTIVE);
        assertThat(entry.getRestoredBy()).isSameAs(user);
    }

    @Test
    @DisplayName("restoreMedicine rejects an already-active entry")
    void restoreMedicine_alreadyActive() {
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.restoreMedicine(entry.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already active");
    }

    @Test
    @DisplayName("mutations reject an unknown entry id")
    void update_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateMedicine(id, new UpdateMedicineRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medicine entry not found.");
    }
}
