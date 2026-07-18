package com.jala.backend.feedinventory.controller;

import com.jala.backend.feedinventory.service.FeedInventoryService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedInventoryController.class)
@Import(WebSecurityTestConfig.class)
class FeedInventoryControllerTest extends WebSliceTestBase {

    @MockitoBean
    private FeedInventoryService feedInventoryService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/feed-inventory"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user lists inventories")
    void list_ok() throws Exception {
        given(feedInventoryService.getAllInventories()).willReturn(List.of());
        mockMvc.perform(get("/api/v1/feed-inventory"))
                .andExpect(status().isOk());
    }
}
