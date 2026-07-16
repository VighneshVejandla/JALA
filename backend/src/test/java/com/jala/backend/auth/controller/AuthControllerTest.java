package com.jala.backend.auth.controller;

import com.jala.backend.auth.dto.LoginRequest;
import com.jala.backend.auth.dto.LoginResponse;
import com.jala.backend.auth.service.AuthService;
import com.jala.backend.common.exception.TooManyRequestsException;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import com.jala.backend.user.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(WebSecurityTestConfig.class)
class AuthControllerTest extends WebSliceTestBase {

    @MockitoBean
    private AuthService authService;

    private static LoginResponse loginResponse() {
        return LoginResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .employeeCode("EMP-001")
                .fullName("Alice Admin")
                .role("ADMIN")
                .build();
    }

    private static UserResponse userResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP-001")
                .fullName("Alice Admin")
                .role("ADMIN")
                .isActive(true)
                .build();
    }

    private LoginRequest validLoginRequest() {

        LoginRequest request = new LoginRequest();
        request.setEmployeeCode("EMP-001");
        request.setPassword("correct-horse-battery-1");
        return request;
    }

    @Test
    @DisplayName("login is public: anonymous with valid body gets 200")
    void login_anonymous_validBody_ok() throws Exception {

        given(authService.login(any(LoginRequest.class)))
                .willReturn(loginResponse());

        String body = objectMapper.writeValueAsString(validLoginRequest());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"));
    }

    @Test
    @DisplayName("login with empty body returns 400 validation envelope")
    void login_invalidBody_badRequest() throws Exception {

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("login with malformed JSON returns 400")
    void login_malformedJson_badRequest() throws Exception {

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    @DisplayName("bad credentials surface as generic 401 envelope")
    void login_badCredentials_unauthorized() throws Exception {

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BadCredentialsException("no such user"));

        String body = objectMapper.writeValueAsString(validLoginRequest());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("rate-limited login returns 429 envelope")
    void login_rateLimited_tooManyRequests() throws Exception {

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new TooManyRequestsException(
                        "Too many login attempts. Try again later."));

        String body = objectMapper.writeValueAsString(validLoginRequest());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Too many login attempts. Try again later."));
    }

    @Test
    @DisplayName("/me without a token is rejected with 401")
    void getCurrentUser_anonymous_unauthorized() throws Exception {

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "EMP-001", roles = "WORKER")
    @DisplayName("/me returns the profile of the authenticated principal")
    void getCurrentUser_authenticated_ok() throws Exception {

        given(authService.getCurrentUser("EMP-001"))
                .willReturn(userResponse());

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Profile fetched successfully"))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-001"));

        then(authService).should().getCurrentUser("EMP-001");
    }
}
