package com.jala.backend.reports.controller;

import com.jala.backend.reports.service.ReportsService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportsController.class)
@Import(WebSecurityTestConfig.class)
class ReportsControllerTest extends WebSliceTestBase {

    @MockitoBean
    private ReportsService reportsService;

    @Test
    @DisplayName("anonymous chart request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/reports/chart/revenue/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker reads the revenue chart")
    void worker_ok() throws Exception {
        given(reportsService.getRevenueChart(any())).willReturn(List.of());
        mockMvc.perform(get("/api/v1/reports/chart/revenue/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("driver is forbidden from reports")
    void driver_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/reports/chart/revenue/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
