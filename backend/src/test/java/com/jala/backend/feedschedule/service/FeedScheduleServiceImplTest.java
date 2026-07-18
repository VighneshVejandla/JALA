package com.jala.backend.feedschedule.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedschedule.dto.request.CreateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.request.UpdateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.response.FeedScheduleResponse;
import com.jala.backend.feedschedule.entity.FeedSchedule;
import com.jala.backend.feedschedule.mapper.FeedScheduleMapper;
import com.jala.backend.feedschedule.repository.FeedScheduleRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.pondcycle.enums.PondCycleStatus;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedScheduleServiceImplTest {

    @Mock
    private FeedScheduleRepository repository;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private FeedScheduleMapper mapper;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private FeedScheduleServiceImpl service;

    private PondCycle cycle;
    private FeedSchedule schedule;

    @BeforeEach
    void setUp() {
        cycle = PondCycle.builder()
                .id(UUID.randomUUID())
                .cycleNumber(1)
                .status(PondCycleStatus.ACTIVE)
                .build();

        schedule = FeedSchedule.builder()
                .id(UUID.randomUUID())
                .pondCycle(cycle)
                .sessionNumber(1)
                .feedingTime(LocalTime.of(6, 0))
                .isActive(true)
                .build();
    }

    private CreateFeedScheduleRequest createRequest(LocalTime... times) {
        CreateFeedScheduleRequest request = new CreateFeedScheduleRequest();
        request.setPondCycleId(cycle.getId());
        request.setFeedingTimes(List.of(times));
        return request;
    }

    private void stubMapperEcho() {
        when(mapper.toResponse(any(FeedSchedule.class)))
                .thenAnswer(inv -> {
                    FeedSchedule s = inv.getArgument(0);
                    return FeedScheduleResponse.builder()
                            .sessionNumber(s.getSessionNumber())
                            .feedingTime(s.getFeedingTime())
                            .isActive(s.getIsActive())
                            .build();
                });
    }

    // ------------------------------------------------------------------
    // createSchedules
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createSchedules sorts times and numbers sessions from 1")
    void createSchedules_success() {
        CreateFeedScheduleRequest request = createRequest(
                LocalTime.of(14, 0),
                LocalTime.of(6, 0),
                LocalTime.of(10, 0));

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(repository.existsByPondCycleIdAndFeedingTime(
                any(UUID.class), any(LocalTime.class)))
                .thenReturn(false);
        stubMapperEcho();

        List<FeedScheduleResponse> result =
                service.createSchedules(request);

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(FeedScheduleResponse::getSessionNumber)
                .containsExactly(1, 2, 3);
        assertThat(result)
                .extracting(FeedScheduleResponse::getFeedingTime)
                .containsExactly(
                        LocalTime.of(6, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(14, 0));
        assertThat(result)
                .extracting(FeedScheduleResponse::getIsActive)
                .containsOnly(true);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FeedSchedule>> captor =
                ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());

        List<FeedSchedule> saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved)
                .allSatisfy(s -> {
                    assertThat(s.getPondCycle()).isSameAs(cycle);
                    assertThat(s.getIsActive()).isTrue();
                });
    }

    @Test
    @DisplayName("createSchedules throws when cycle does not exist")
    void createSchedules_cycleNotFound() {
        CreateFeedScheduleRequest request =
                createRequest(LocalTime.of(6, 0));

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSchedules(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond cycle not found");

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createSchedules rejects a harvested cycle")
    void createSchedules_cycleNotActive() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        CreateFeedScheduleRequest request =
                createRequest(LocalTime.of(6, 0));

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.createSchedules(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(
                        "Feed schedules can only be created for active cycles.");

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createSchedules rejects duplicate times in the request")
    void createSchedules_duplicateTimesInRequest() {
        CreateFeedScheduleRequest request = createRequest(
                LocalTime.of(6, 0),
                LocalTime.of(6, 0));

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.createSchedules(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Duplicate feeding times are not allowed.");

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createSchedules rejects a time already stored for the cycle")
    void createSchedules_timeAlreadyExists() {
        CreateFeedScheduleRequest request = createRequest(
                LocalTime.of(6, 0),
                LocalTime.of(10, 0));

        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(repository.existsByPondCycleIdAndFeedingTime(
                cycle.getId(), LocalTime.of(6, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createSchedules(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Feeding time already exists.");

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createSchedules propagates access denial before repository access")
    void createSchedules_accessDenied() {
        CreateFeedScheduleRequest request =
                createRequest(LocalTime.of(6, 0));

        doThrow(new AccessDeniedException("denied"))
                .when(siteAccessService)
                .checkPondCycleAccess(cycle.getId());

        assertThatThrownBy(() -> service.createSchedules(request))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(pondCycleRepository, repository);
    }

    // ------------------------------------------------------------------
    // getSchedulesByCycle
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getSchedulesByCycle maps repository results")
    void getSchedulesByCycle_success() {
        FeedScheduleResponse response =
                FeedScheduleResponse.builder().id(schedule.getId()).build();

        when(repository.findByPondCycleIdOrderBySessionNumber(
                cycle.getId()))
                .thenReturn(List.of(schedule));
        when(mapper.toResponse(schedule)).thenReturn(response);

        List<FeedScheduleResponse> result =
                service.getSchedulesByCycle(cycle.getId());

        assertThat(result).containsExactly(response);
        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
    }

    // ------------------------------------------------------------------
    // updateSchedule
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateSchedule throws when schedule does not exist")
    void updateSchedule_notFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();

        assertThatThrownBy(() -> service.updateSchedule(id, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Feed schedule not found");
    }

    @Test
    @DisplayName("updateSchedule rejects schedules of harvested cycles")
    void updateSchedule_harvestedCycle() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        UUID id = schedule.getId();

        when(repository.findById(id)).thenReturn(Optional.of(schedule));

        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();

        assertThatThrownBy(() -> service.updateSchedule(id, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(
                        "Cannot update feed schedule for a harvested cycle.");

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateSchedule rejects a feeding time used by another session")
    void updateSchedule_feedingTimeConflict() {
        UUID id = schedule.getId();

        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();
        request.setFeedingTime(LocalTime.of(9, 30));

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.existsByPondCycleIdAndFeedingTime(
                cycle.getId(), LocalTime.of(9, 30)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.updateSchedule(id, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Feeding time already exists.");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateSchedule skips the duplicate check for an unchanged time")
    void updateSchedule_sameFeedingTime() {
        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();
        request.setFeedingTime(schedule.getFeedingTime());

        when(repository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));
        when(repository.save(schedule)).thenReturn(schedule);
        when(mapper.toResponse(schedule))
                .thenReturn(FeedScheduleResponse.builder().build());

        service.updateSchedule(schedule.getId(), request);

        verify(repository, never())
                .existsByPondCycleIdAndFeedingTime(any(), any());
        assertThat(schedule.getFeedingTime())
                .isEqualTo(LocalTime.of(6, 0));
        verify(repository).save(schedule);
    }

    @Test
    @DisplayName("updateSchedule applies a new non-conflicting feeding time")
    void updateSchedule_newTime() {
        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();
        request.setFeedingTime(LocalTime.of(18, 0));

        when(repository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));
        when(repository.existsByPondCycleIdAndFeedingTime(
                cycle.getId(), LocalTime.of(18, 0)))
                .thenReturn(false);
        when(repository.save(schedule)).thenReturn(schedule);
        when(mapper.toResponse(schedule))
                .thenReturn(FeedScheduleResponse.builder().build());

        service.updateSchedule(schedule.getId(), request);

        assertThat(schedule.getFeedingTime())
                .isEqualTo(LocalTime.of(18, 0));
        verify(repository).save(schedule);
    }

    @Test
    @DisplayName("updateSchedule toggles isActive without touching the time")
    void updateSchedule_isActiveOnly() {
        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();
        request.setIsActive(false);

        when(repository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));
        when(repository.save(schedule)).thenReturn(schedule);
        when(mapper.toResponse(schedule))
                .thenReturn(FeedScheduleResponse.builder().build());

        service.updateSchedule(schedule.getId(), request);

        assertThat(schedule.getIsActive()).isFalse();
        assertThat(schedule.getFeedingTime())
                .isEqualTo(LocalTime.of(6, 0));
        verify(repository, never())
                .existsByPondCycleIdAndFeedingTime(any(), any());
    }

    @Test
    @DisplayName("updateSchedule with an empty request saves unchanged entity")
    void updateSchedule_noFields() {
        UpdateFeedScheduleRequest request = new UpdateFeedScheduleRequest();

        when(repository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));
        when(repository.save(schedule)).thenReturn(schedule);
        when(mapper.toResponse(schedule))
                .thenReturn(FeedScheduleResponse.builder().build());

        service.updateSchedule(schedule.getId(), request);

        assertThat(schedule.getFeedingTime())
                .isEqualTo(LocalTime.of(6, 0));
        assertThat(schedule.getIsActive()).isTrue();
        verify(repository).save(schedule);
    }

    // ------------------------------------------------------------------
    // deactivateSchedule / activateSchedule
    // ------------------------------------------------------------------

    @Test
    @DisplayName("deactivateSchedule sets isActive false and saves")
    void deactivateSchedule_success() {
        when(repository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));

        service.deactivateSchedule(schedule.getId());

        assertThat(schedule.getIsActive()).isFalse();
        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        verify(repository).save(schedule);
    }

    @Test
    @DisplayName("activateSchedule sets isActive true and saves")
    void activateSchedule_success() {
        schedule.setIsActive(false);

        when(repository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));

        service.activateSchedule(schedule.getId());

        assertThat(schedule.getIsActive()).isTrue();
        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        verify(repository).save(schedule);
    }

    @Test
    @DisplayName("activateSchedule throws when schedule does not exist")
    void activateSchedule_notFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateSchedule(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Feed schedule not found");
    }
}
