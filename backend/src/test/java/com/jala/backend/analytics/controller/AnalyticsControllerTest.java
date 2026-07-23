package com.jala.backend.analytics.controller;

import com.jala.backend.analytics.dto.response.FeedAnalyticsResponse;
import com.jala.backend.analytics.service.AnalyticsService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@Import(WebSecurityTestConfig.class)
class AnalyticsControllerTest extends WebSliceTestBase {

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("anonymous request is rejected with 401")
    void anonymous_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/feed/pond/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("a role outside ADMIN/WORKER is forbidden")
    void driver_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/feed/pond/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker can read pond feed analytics")
    void worker_ok() throws Exception {
        given(analyticsService.getPondFeedAnalytics(any()))
                .willReturn(FeedAnalyticsResponse.builder().build());

        mockMvc.perform(get("/api/v1/analytics/feed/pond/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("non-UUID path variable yields a 400 envelope")
    void badUuid_badRequest() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/feed/pond/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker can read the daily feed series")
    void dailyFeed_ok() throws Exception {
        given(analyticsService.getSiteFeedDaily(any(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/analytics/feed/site/" + UUID.randomUUID() + "/daily")
                        .param("days", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
