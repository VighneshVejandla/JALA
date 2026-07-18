package com.jala.backend.site.controller;

import com.jala.backend.site.dto.request.CreateSiteRequest;
import com.jala.backend.site.dto.response.SiteResponse;
import com.jala.backend.site.service.SiteService;
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

@WebMvcTest(controllers = SiteController.class)
@Import(WebSecurityTestConfig.class)
class SiteControllerTest extends WebSliceTestBase {

    @MockitoBean
    private SiteService siteService;

    private CreateSiteRequest validRequest() {
        CreateSiteRequest r = new CreateSiteRequest();
        r.setSiteCode("S-001");
        r.setSiteName("Site 1");
        r.setOwnerName("Owner");
        r.setLocation("Loc");
        r.setTotalAcres(new BigDecimal("10.00"));
        return r;
    }

    @Test
    @DisplayName("anonymous list request is rejected with 401")
    void list_anonymous_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/sites"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated worker can list sites")
    void list_worker_ok() throws Exception {
        given(siteService.getAllSites()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("create site requires ADMIN")
    void create_worker_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin create returns 201 with envelope")
    void create_admin_created() throws Exception {
        given(siteService.createSite(any()))
                .willReturn(SiteResponse.builder()
                        .id(UUID.randomUUID()).siteCode("S-001").build());

        mockMvc.perform(post("/api/v1/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Site created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("invalid create body returns 400 validation envelope")
    void create_invalidBody_badRequest() throws Exception {
        mockMvc.perform(post("/api/v1/sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
