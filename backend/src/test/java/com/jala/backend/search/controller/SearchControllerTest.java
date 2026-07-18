package com.jala.backend.search.controller;

import com.jala.backend.search.dto.response.GlobalSearchResponse;
import com.jala.backend.search.service.SearchService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
@Import(WebSecurityTestConfig.class)
class SearchControllerTest extends WebSliceTestBase {

    @MockitoBean
    private SearchService searchService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("keyword", "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user searches")
    void search_ok() throws Exception {
        given(searchService.search(any()))
                .willReturn(GlobalSearchResponse.builder().build());
        mockMvc.perform(get("/api/v1/search").param("keyword", "shrimp"))
                .andExpect(status().isOk());
    }
}
