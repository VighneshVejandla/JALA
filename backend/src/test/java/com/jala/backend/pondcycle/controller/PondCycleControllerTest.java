package com.jala.backend.pondcycle.controller;

import com.jala.backend.pondcycle.dto.response.PondCycleResponse;
import com.jala.backend.pondcycle.service.PondCycleService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PondCycleController.class)
@Import(WebSecurityTestConfig.class)
class PondCycleControllerTest extends WebSliceTestBase {

    @MockitoBean
    private PondCycleService pondCycleService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/pond-cycles/pond/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user lists cycles for a pond")
    void listByPond_ok() throws Exception {
        given(pondCycleService.getCyclesByPond(any())).willReturn(List.of());
        mockMvc.perform(get("/api/v1/pond-cycles/pond/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user reads the active cycle")
    void active_ok() throws Exception {
        given(pondCycleService.getActiveCycle(any()))
                .willReturn(PondCycleResponse.builder().build());
        mockMvc.perform(get("/api/v1/pond-cycles/active/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
