package com.jala.backend.pond.controller;

import com.jala.backend.pond.dto.request.CreatePondRequest;
import com.jala.backend.pond.dto.response.PondResponse;
import com.jala.backend.pond.service.PondService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PondController.class)
@Import(WebSecurityTestConfig.class)
class PondControllerTest extends WebSliceTestBase {

    @MockitoBean
    private PondService pondService;

    private static PondResponse pondResponse() {
        return PondResponse.builder()
                .id(UUID.randomUUID())
                .pondCode("P-001")
                .pondName("Pond 1")
                .build();
    }

    @Test
    @DisplayName("anonymous request is rejected with 401")
    void getPonds_anonymous_unauthorized() throws Exception {

        mockMvc.perform(get("/api/v1/ponds"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("create pond requires ADMIN role")
    void createPond_worker_forbidden() throws Exception {

        // Body must be valid: request validation (400) runs before
        // method security (403).
        String body = objectMapper.writeValueAsString(
                validCreateRequest());

        mockMvc.perform(post("/api/v1/ponds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create pond returns 201 with envelope")
    void createPond_admin_created() throws Exception {

        given(pondService.createPond(any(CreatePondRequest.class)))
                .willReturn(pondResponse());

        String body = objectMapper.writeValueAsString(
                validCreateRequest());

        mockMvc.perform(post("/api/v1/ponds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Pond created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("invalid create body returns 400 validation envelope")
    void createPond_invalidBody_badRequest() throws Exception {

        mockMvc.perform(post("/api/v1/ponds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated worker can list ponds")
    void getPonds_worker_ok() throws Exception {

        given(pondService.getAllPonds()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/ponds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private CreatePondRequest validCreateRequest() {

        CreatePondRequest request = new CreatePondRequest();
        request.setSiteId(UUID.randomUUID());
        request.setPondCode("P-001");
        request.setPondName("Pond 1");
        request.setPondAcres(BigDecimal.ONE);
        return request;
    }
}
