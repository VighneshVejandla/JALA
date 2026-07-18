package com.jala.backend.dashboard.controller;

import com.jala.backend.dashboard.dto.response.PondDashboardResponse;
import com.jala.backend.dashboard.service.PondDashboardService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PondDashboardController.class)
@Import(WebSecurityTestConfig.class)
class PondDashboardControllerTest extends WebSliceTestBase {

    @MockitoBean
    private PondDashboardService pondDashboardService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user reads the pond dashboard")
    void dashboard_ok() throws Exception {
        given(pondDashboardService.getDashboard(any()))
                .willReturn(PondDashboardResponse.builder().build());
        mockMvc.perform(get("/api/v1/dashboard/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
