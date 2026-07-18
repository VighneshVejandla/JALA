package com.jala.backend.feeddelivery.controller;

import com.jala.backend.feeddelivery.service.FeedDeliveryService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedDeliveryController.class)
@Import(WebSecurityTestConfig.class)
class FeedDeliveryControllerTest extends WebSliceTestBase {

    @MockitoBean
    private FeedDeliveryService feedDeliveryService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/feed-deliveries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("manager can list deliveries")
    void manager_ok() throws Exception {
        given(feedDeliveryService.getAllDeliveries(any(), any()))
                .willReturn(List.of());
        mockMvc.perform(get("/api/v1/feed-deliveries"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker is forbidden from the delivery list")
    void worker_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/feed-deliveries"))
                .andExpect(status().isForbidden());
    }
}
