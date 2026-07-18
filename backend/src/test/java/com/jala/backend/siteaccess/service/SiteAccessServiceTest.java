package com.jala.backend.siteaccess.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.siteaccess.repository.UserSiteRepository;
import com.jala.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteAccessServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserSiteRepository userSiteRepository;

    @Mock
    private PondRepository pondRepository;

    @Mock
    private PondCycleRepository pondCycleRepository;

    @InjectMocks
    private SiteAccessService siteAccessService;

    private final UUID siteId = UUID.randomUUID();
    private final UUID pondId = UUID.randomUUID();
    private final UUID pondCycleId = UUID.randomUUID();

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP001")
                .fullName("Test User")
                .passwordHash("hash")
                .build();
    }

    private void restrictedUser() {
        when(currentUserService.isUnrestricted()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void checkSiteAccessBypassesForUnrestrictedUser() {
        when(currentUserService.isUnrestricted()).thenReturn(true);

        assertThatCode(() -> siteAccessService.checkSiteAccess(siteId))
                .doesNotThrowAnyException();

        verifyNoInteractions(userSiteRepository, pondRepository,
                pondCycleRepository);
    }

    @Test
    void checkSiteAccessPassesWhenSiteAssigned() {
        restrictedUser();
        when(userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId))
                .thenReturn(true);

        assertThatCode(() -> siteAccessService.checkSiteAccess(siteId))
                .doesNotThrowAnyException();
    }

    @Test
    void checkSiteAccessThrowsWhenSiteNotAssigned() {
        restrictedUser();
        when(userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId))
                .thenReturn(false);

        assertThatThrownBy(() -> siteAccessService.checkSiteAccess(siteId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have access to this site");
    }

    @Test
    void checkPondAccessBypassesForUnrestrictedUser() {
        when(currentUserService.isUnrestricted()).thenReturn(true);

        assertThatCode(() -> siteAccessService.checkPondAccess(pondId))
                .doesNotThrowAnyException();

        verifyNoInteractions(pondRepository, userSiteRepository);
    }

    @Test
    void checkPondAccessPassesWhenParentSiteAssigned() {
        restrictedUser();
        when(pondRepository.findSiteIdByPondId(pondId))
                .thenReturn(Optional.of(siteId));
        when(userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId))
                .thenReturn(true);

        assertThatCode(() -> siteAccessService.checkPondAccess(pondId))
                .doesNotThrowAnyException();
    }

    @Test
    void checkPondAccessThrowsWhenPondNotFound() {
        when(currentUserService.isUnrestricted()).thenReturn(false);
        when(pondRepository.findSiteIdByPondId(pondId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteAccessService.checkPondAccess(pondId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond not found.");

        verifyNoInteractions(userSiteRepository);
    }

    @Test
    void checkPondAccessThrowsWhenParentSiteNotAssigned() {
        restrictedUser();
        when(pondRepository.findSiteIdByPondId(pondId))
                .thenReturn(Optional.of(siteId));
        when(userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId))
                .thenReturn(false);

        assertThatThrownBy(() -> siteAccessService.checkPondAccess(pondId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have access to this site");
    }

    @Test
    void checkPondCycleAccessBypassesForUnrestrictedUser() {
        when(currentUserService.isUnrestricted()).thenReturn(true);

        assertThatCode(() ->
                siteAccessService.checkPondCycleAccess(pondCycleId))
                .doesNotThrowAnyException();

        verifyNoInteractions(pondCycleRepository, userSiteRepository);
    }

    @Test
    void checkPondCycleAccessPassesWhenParentSiteAssigned() {
        restrictedUser();
        when(pondCycleRepository.findSiteIdByCycleId(pondCycleId))
                .thenReturn(Optional.of(siteId));
        when(userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId))
                .thenReturn(true);

        assertThatCode(() ->
                siteAccessService.checkPondCycleAccess(pondCycleId))
                .doesNotThrowAnyException();
    }

    @Test
    void checkPondCycleAccessThrowsWhenCycleNotFound() {
        when(currentUserService.isUnrestricted()).thenReturn(false);
        when(pondCycleRepository.findSiteIdByCycleId(pondCycleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                siteAccessService.checkPondCycleAccess(pondCycleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pond cycle not found.");

        verifyNoInteractions(userSiteRepository);
    }

    @Test
    void checkPondCycleAccessThrowsWhenParentSiteNotAssigned() {
        restrictedUser();
        when(pondCycleRepository.findSiteIdByCycleId(pondCycleId))
                .thenReturn(Optional.of(siteId));
        when(userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                siteAccessService.checkPondCycleAccess(pondCycleId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have access to this site");
    }

    @Test
    void accessibleSiteIdsReturnsNullForUnrestrictedUser() {
        when(currentUserService.isUnrestricted()).thenReturn(true);

        assertThat(siteAccessService.accessibleSiteIds()).isNull();

        verifyNoInteractions(userSiteRepository);
    }

    @Test
    void accessibleSiteIdsReturnsAssignedSitesForRestrictedUser() {
        restrictedUser();
        List<UUID> assigned = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(userSiteRepository.findSiteIdsByUserId(user.getId()))
                .thenReturn(assigned);

        assertThat(siteAccessService.accessibleSiteIds())
                .isEqualTo(assigned);
    }
}
