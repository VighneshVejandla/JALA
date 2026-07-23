package com.jala.backend.auth.service;

import com.jala.backend.auth.dto.LoginRequest;
import com.jala.backend.auth.dto.LoginResponse;
import com.jala.backend.auth.jwt.JwtService;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.common.exception.TooManyRequestsException;
import com.jala.backend.config.JwtProperties;
import com.jala.backend.role.entity.Role;
import com.jala.backend.security.model.CustomUserDetails;
import com.jala.backend.user.dto.response.UserResponse;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.mapper.UserMapper;
import com.jala.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private static User buildUser() {

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .description("Administrator")
                .build();

        return User.builder()
                .id(UUID.randomUUID())
                .role(role)
                .employeeCode("EMP-001")
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("9999999999")
                .passwordHash("hash")
                .isActive(true)
                .tokenVersion(0)
                .build();
    }

    private static LoginRequest loginRequest() {

        LoginRequest request = new LoginRequest();
        request.setEmployeeCode("EMP-001");
        request.setPassword("secret-password1");
        return request;
    }

    @Test
    @DisplayName("login success returns token response and resets attempts")
    void login_success() {

        User user = buildUser();
        LoginRequest request = loginRequest();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal())
                .thenReturn(new CustomUserDetails(user));

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(any(CustomUserDetails.class)))
                .thenReturn("jwt-token");

        when(jwtProperties.getExpiration()).thenReturn(3_600_000L);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getEmployeeCode()).isEqualTo("EMP-001");
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(loginAttemptService).checkNotLocked("EMP-001");
        verify(loginAttemptService).reset("EMP-001");
        verify(loginAttemptService, never()).recordFailure(any());
    }

    @Test
    @DisplayName("login failure records the failure and rethrows")
    void login_badCredentials_recordsFailureAndRethrows() {

        LoginRequest request = loginRequest();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verify(loginAttemptService).recordFailure("EMP-001");
        verify(loginAttemptService, never()).reset(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("locked account fails fast before authentication")
    void login_locked_throwsBeforeAuthenticate() {

        LoginRequest request = loginRequest();

        org.mockito.Mockito.doThrow(
                        new TooManyRequestsException("Too many failed login attempts. Please try again later."))
                .when(loginAttemptService).checkNotLocked("EMP-001");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(TooManyRequestsException.class);

        verifyNoInteractions(authenticationManager);
        verify(loginAttemptService, never()).recordFailure(any());
        verify(loginAttemptService, never()).reset(any());
    }

    @Test
    @DisplayName("getCurrentUser returns mapped response when user exists")
    void getCurrentUser_found() {

        User user = buildUser();

        UserResponse mapped = UserResponse.builder()
                .id(user.getId())
                .employeeCode("EMP-001")
                .fullName("Jane Doe")
                .role("ADMIN")
                .isActive(true)
                .build();

        when(userRepository.findByEmployeeCode("EMP-001"))
                .thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(mapped);

        UserResponse response = authService.getCurrentUser("EMP-001");

        assertThat(response).isSameAs(mapped);
        assertThat(response.getEmployeeCode()).isEqualTo("EMP-001");
    }

    @Test
    @DisplayName("getCurrentUser throws when user does not exist")
    void getCurrentUser_notFound() {

        when(userRepository.findByEmployeeCode("GHOST"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("GHOST"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("updateProfile applies non-null fields and saves")
    void updateProfile_updatesFields() {
        User user = buildUser();
        when(userRepository.findByEmployeeCode("EMP-001"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(UserResponse.builder().build());

        var req = new com.jala.backend.auth.dto.UpdateProfileRequest();
        req.setFullName("New Name");
        req.setEmail("new@example.com");
        req.setPhone("8888888888");

        authService.updateProfile("EMP-001", req);

        assertThat(user.getFullName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPhone()).isEqualTo("8888888888");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateProfile rejects an unknown user")
    void updateProfile_notFound() {
        when(userRepository.findByEmployeeCode("GHOST"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                authService.updateProfile("GHOST",
                        new com.jala.backend.auth.dto.UpdateProfileRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("changePassword re-encodes when the current password matches")
    void changePassword_success() {
        User user = buildUser();
        when(userRepository.findByEmployeeCode("EMP-001"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-pass-123", "hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password-123")).thenReturn("new-hash");

        var req = new com.jala.backend.auth.dto.ChangePasswordRequest();
        req.setCurrentPassword("old-pass-123");
        req.setNewPassword("new-password-123");

        authService.changePassword("EMP-001", req);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword rejects a wrong current password")
    void changePassword_wrongCurrent() {
        User user = buildUser();
        when(userRepository.findByEmployeeCode("EMP-001"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        var req = new com.jala.backend.auth.dto.ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("new-password-123");

        assertThatThrownBy(() -> authService.changePassword("EMP-001", req))
                .isInstanceOf(com.jala.backend.common.exception.BadRequestException.class);
        verify(userRepository, never()).save(any());
    }
}
