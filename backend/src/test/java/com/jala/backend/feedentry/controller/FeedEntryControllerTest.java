package com.jala.backend.feedentry.controller;

import com.jala.backend.feedentry.dto.request.CreateFeedEntryRequest;
import com.jala.backend.feedentry.dto.response.FeedEntryResponse;
import com.jala.backend.feedentry.enums.FeedSize;
import com.jala.backend.feedentry.service.FeedEntryService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedEntryController.class)
@Import(WebSecurityTestConfig.class)
class FeedEntryControllerTest extends WebSliceTestBase {

    @MockitoBean
    private FeedEntryService feedEntryService;

    private CreateFeedEntryRequest validCreate() {
        CreateFeedEntryRequest r = new CreateFeedEntryRequest();
        r.setPondCycleId(UUID.randomUUID());
        r.setFeedScheduleId(UUID.randomUUID());
        r.setFeedDate(LocalDate.now());
        r.setFeedSize(FeedSize.ONE);
        r.setFeedQuantityKg(new BigDecimal("10.0"));
        return r;
    }

    @Test
    @DisplayName("anonymous list is 401")
    void list_anon() throws Exception {
        mockMvc.perform(get("/api/v1/feed-entries")
                        .param("pondCycleId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker lists feed entries")
    void list_worker_ok() throws Exception {
        given(feedEntryService.getFeedEntries(any(), any()))
                .willReturn(List.of());
        mockMvc.perform(get("/api/v1/feed-entries")
                        .param("pondCycleId", UUID.randomUUID().toString())
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker creates a feed entry (201)")
    void create_worker_created() throws Exception {
        given(feedEntryService.createFeedEntry(any()))
                .willReturn(FeedEntryResponse.builder().build());
        mockMvc.perform(post("/api/v1/feed-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("driver cannot create a feed entry (403)")
    void create_driver_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/feed-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate())))
                .andExpect(status().isForbidden());
    }
}
