package com.jala.backend.harvest.controller;

import com.jala.backend.harvest.service.HarvestService;
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

import com.jala.backend.harvest.dto.response.HarvestResponse;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HarvestController.class)
@Import(WebSecurityTestConfig.class)
class HarvestControllerTest extends WebSliceTestBase {

    @MockitoBean
    private HarvestService harvestService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/harvests")
                        .param("pondCycleId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user lists harvests")
    void list_ok() throws Exception {
        given(harvestService.getHarvests(any(), any(), any())).willReturn(List.of());
        mockMvc.perform(get("/api/v1/harvests")
                        .param("pondCycleId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin edits a harvest")
    void update_ok() throws Exception {
        UUID id = UUID.randomUUID();
        HarvestResponse resp = new HarvestResponse();
        resp.setId(id);
        given(harvestService.updateHarvest(eq(id), any())).willReturn(resp);
        mockMvc.perform(patch("/api/v1/harvests/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"harvestQuantityKg\":320,\"buyerName\":\"Acme\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Harvest updated successfully"));
    }
}
