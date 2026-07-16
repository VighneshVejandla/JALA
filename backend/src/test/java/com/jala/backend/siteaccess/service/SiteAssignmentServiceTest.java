package com.jala.backend.siteaccess.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.entity.UserSite;
import com.jala.backend.siteaccess.repository.UserSiteRepository;
import com.jala.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteAssignmentServiceTest {

    @Mock
    private UserSiteRepository userSiteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private SiteAssignmentService siteAssignmentService;

    private UUID userId;
    private UUID siteId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        siteId = UUID.randomUUID();
    }

    @Test
    @DisplayName("assignSite persists a new assignment")
    void assignSite_success() {

        when(userRepository.existsById(userId)).thenReturn(true);
        when(siteRepository.existsById(siteId)).thenReturn(true);
        when(userSiteRepository.existsByUserIdAndSiteId(userId, siteId))
                .thenReturn(false);

        siteAssignmentService.assignSite(userId, siteId);

        ArgumentCaptor<UserSite> captor =
                ArgumentCaptor.forClass(UserSite.class);
        verify(userSiteRepository).save(captor.capture());

        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getSiteId()).isEqualTo(siteId);
    }

    @Test
    @DisplayName("assignSite rejects unknown users")
    void assignSite_userMissing() {

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() ->
                siteAssignmentService.assignSite(userId, siteId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userSiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignSite rejects unknown sites")
    void assignSite_siteMissing() {

        when(userRepository.existsById(userId)).thenReturn(true);
        when(siteRepository.existsById(siteId)).thenReturn(false);

        assertThatThrownBy(() ->
                siteAssignmentService.assignSite(userId, siteId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Site not found.");

        verify(userSiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignSite silently skips existing assignments")
    void assignSite_duplicateSkipped() {

        when(userRepository.existsById(userId)).thenReturn(true);
        when(siteRepository.existsById(siteId)).thenReturn(true);
        when(userSiteRepository.existsByUserIdAndSiteId(userId, siteId))
                .thenReturn(true);

        siteAssignmentService.assignSite(userId, siteId);

        verify(userSiteRepository, never()).save(any());
    }

    @Test
    @DisplayName("unassignSite deletes the assignment")
    void unassignSite_deletes() {

        siteAssignmentService.unassignSite(userId, siteId);

        verify(userSiteRepository).deleteByUserIdAndSiteId(userId, siteId);
    }

    @Test
    @DisplayName("getAssignedSiteIds returns the repository result")
    void getAssignedSiteIds_success() {

        List<UUID> siteIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userSiteRepository.findSiteIdsByUserId(userId))
                .thenReturn(siteIds);

        assertThat(siteAssignmentService.getAssignedSiteIds(userId))
                .isSameAs(siteIds);
    }

    @Test
    @DisplayName("getAssignedSiteIds rejects unknown users")
    void getAssignedSiteIds_userMissing() {

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() ->
                siteAssignmentService.getAssignedSiteIds(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userSiteRepository, never()).findSiteIdsByUserId(any());
    }
}
