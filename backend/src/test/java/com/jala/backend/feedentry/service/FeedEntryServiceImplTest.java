package com.jala.backend.feedentry.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.feedentry.dto.request.CreateFeedEntryRequest;
import com.jala.backend.feedentry.dto.request.UpdateFeedEntryRequest;
import com.jala.backend.feedentry.dto.response.FeedEntryResponse;
import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.enums.FeedEntryStatus;
import com.jala.backend.feedentry.enums.FeedSize;
import com.jala.backend.feedentry.mapper.FeedEntryMapper;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.feedschedule.entity.FeedSchedule;
import com.jala.backend.feedschedule.repository.FeedScheduleRepository;
import com.jala.backend.notification.service.NotificationService;
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
class FeedEntryServiceImplTest {

    @Mock
    private FeedEntryRepository repository;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @Mock
    private FeedScheduleRepository feedScheduleRepository;

    @Mock
    private FeedEntryMapper mapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SiteAccessService siteAccessService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FeedEntryServiceImpl service;

    private PondCycle cycle;
    private FeedSchedule schedule;
    private FeedEntry entry;

    @BeforeEach
    void setUp() {
        Site site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").build();
        Pond pond = Pond.builder()
                .id(UUID.randomUUID()).site(site).pondCode("P-001").build();
        cycle = PondCycle.builder()
                .id(UUID.randomUUID()).pond(pond)
                .status(PondCycleStatus.ACTIVE).build();
        schedule = FeedSchedule.builder()
                .id(UUID.randomUUID()).pondCycle(cycle).sessionNumber(1).build();
        entry = FeedEntry.builder()
                .id(UUID.randomUUID()).pondCycle(cycle).feedSchedule(schedule)
                .status(FeedEntryStatus.ACTIVE).build();
    }

    private CreateFeedEntryRequest createRequest() {
        CreateFeedEntryRequest r = new CreateFeedEntryRequest();
        r.setPondCycleId(cycle.getId());
        r.setFeedScheduleId(schedule.getId());
        r.setFeedDate(LocalDate.now());
        r.setFeedSize(FeedSize.ONE);
        r.setFeedQuantityKg(new BigDecimal("10.00"));
        return r;
    }

    @Test
    @DisplayName("createFeedEntry persists and raises a notification")
    void createFeedEntry_success() {
        CreateFeedEntryRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(feedScheduleRepository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));
        when(repository.existsByPondCycleIdAndFeedScheduleIdAndFeedDate(
                any(), any(), any())).thenReturn(false);
        when(currentUserService.getCurrentUser())
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(mapper.toEntity(request)).thenReturn(entry);
        when(repository.save(entry)).thenReturn(entry);
        when(mapper.toResponse(entry))
                .thenReturn(FeedEntryResponse.builder().build());

        service.createFeedEntry(request);

        verify(siteAccessService).checkPondCycleAccess(cycle.getId());
        verify(notificationService).createFeedNotification(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("createFeedEntry rejects a non-active cycle")
    void createFeedEntry_inactiveCycle() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        CreateFeedEntryRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> service.createFeedEntry(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("active cycles");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("createFeedEntry rejects a schedule from another cycle")
    void createFeedEntry_scheduleMismatch() {
        PondCycle other = PondCycle.builder().id(UUID.randomUUID()).build();
        schedule.setPondCycle(other);
        CreateFeedEntryRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(feedScheduleRepository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> service.createFeedEntry(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    @DisplayName("createFeedEntry rejects a duplicate session entry")
    void createFeedEntry_duplicate() {
        CreateFeedEntryRequest request = createRequest();
        when(pondCycleRepository.findById(cycle.getId()))
                .thenReturn(Optional.of(cycle));
        when(feedScheduleRepository.findById(schedule.getId()))
                .thenReturn(Optional.of(schedule));
        when(repository.existsByPondCycleIdAndFeedScheduleIdAndFeedDate(
                any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.createFeedEntry(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already entered");
    }

    @Test
    @DisplayName("updateFeedEntry rejects a harvested cycle")
    void updateFeedEntry_harvestedCycle() {
        cycle.setStatus(PondCycleStatus.HARVESTED);
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() ->
                service.updateFeedEntry(entry.getId(), new UpdateFeedEntryRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("harvested cycle");
    }

    @Test
    @DisplayName("updateFeedEntry applies provided fields")
    void updateFeedEntry_success() {
        UpdateFeedEntryRequest request = new UpdateFeedEntryRequest();
        request.setFeedQuantityKg(new BigDecimal("12.50"));
        request.setRemarks("topped up");

        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(repository.save(entry)).thenReturn(entry);
        when(mapper.toResponse(entry))
                .thenReturn(FeedEntryResponse.builder().build());

        service.updateFeedEntry(entry.getId(), request);

        assertThat(entry.getFeedQuantityKg()).isEqualByComparingTo("12.50");
        assertThat(entry.getRemarks()).isEqualTo("topped up");
    }

    @Test
    @DisplayName("cancelFeedEntry records who/why and flips status")
    void cancelFeedEntry_success() {
        User user = User.builder().id(UUID.randomUUID()).build();
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(currentUserService.getCurrentUser()).thenReturn(user);

        service.cancelFeedEntry(entry.getId(), "mistake");

        assertThat(entry.getStatus()).isEqualTo(FeedEntryStatus.CANCELLED);
        assertThat(entry.getCancelledBy()).isSameAs(user);
        assertThat(entry.getCancellationReason()).isEqualTo("mistake");
    }

    @Test
    @DisplayName("cancelFeedEntry rejects an already-cancelled entry")
    void cancelFeedEntry_alreadyCancelled() {
        entry.setStatus(FeedEntryStatus.CANCELLED);
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.cancelFeedEntry(entry.getId(), "x"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("restoreFeedEntry reactivates a cancelled entry")
    void restoreFeedEntry_success() {
        entry.setStatus(FeedEntryStatus.CANCELLED);
        when(repository.findById(entry.getId())).thenReturn(Optional.of(entry));

        service.restoreFeedEntry(entry.getId());

        assertThat(entry.getStatus()).isEqualTo(FeedEntryStatus.ACTIVE);
        assertThat(entry.getCancelledBy()).isNull();
    }

    @Test
    @DisplayName("unknown entry id is rejected")
    void cancel_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelFeedEntry(id, "x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Feed entry not found");
    }
}
