package com.jala.backend.security.service;

import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String employeeCode, String... authorities) {
        List<SimpleGrantedAuthority> granted = java.util.Arrays
                .stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        employeeCode, null, granted));
    }

    @Test
    void getCurrentUserReturnsAuthenticatedUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP001")
                .fullName("Test User")
                .passwordHash("hash")
                .build();

        authenticateAs("EMP001");
        when(userRepository.findByEmployeeCode("EMP001"))
                .thenReturn(Optional.of(user));

        assertThat(currentUserService.getCurrentUser()).isSameAs(user);
    }

    @Test
    void getCurrentUserThrowsWhenNoAuthenticationPresent() {
        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No authenticated user");

        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUserThrowsWhenAuthenticationIsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("EMP001", null));

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No authenticated user");

        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUserThrowsWhenUserNoLongerExists() {
        authenticateAs("GHOST");
        when(userRepository.findByEmployeeCode("GHOST"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authenticated user no longer exists");
    }

    @Test
    void isUnrestrictedReturnsTrueForAdmin() {
        authenticateAs("EMP001", "ROLE_ADMIN");

        assertThat(currentUserService.isUnrestricted()).isTrue();
    }

    @Test
    void isUnrestrictedReturnsTrueForManager() {
        authenticateAs("EMP001", "ROLE_MANAGER");

        assertThat(currentUserService.isUnrestricted()).isTrue();
    }

    @Test
    void isUnrestrictedReturnsFalseForRestrictedRoles() {
        authenticateAs("EMP001", "ROLE_WORKER", "ROLE_SUPERVISOR",
                "ROLE_DRIVER");

        assertThat(currentUserService.isUnrestricted()).isFalse();
    }

    @Test
    void isUnrestrictedReturnsFalseWithoutAuthorities() {
        authenticateAs("EMP001");

        assertThat(currentUserService.isUnrestricted()).isFalse();
    }

    @Test
    void isUnrestrictedReturnsFalseWhenNoAuthenticationPresent() {
        assertThat(currentUserService.isUnrestricted()).isFalse();
    }
}
