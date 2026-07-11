package com.jala.backend.search.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.common.response.ApiResponse;
import com.jala.backend.search.dto.response.GlobalSearchResponse;
import com.jala.backend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.SEARCH_BASE_URL)
@RequiredArgsConstructor
public class SearchController {

    private final SearchService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>>
    search(
            @RequestParam String keyword) {

        GlobalSearchResponse response =
                service.search(keyword);

        return ResponseEntity.ok(
                ApiResponse.<GlobalSearchResponse>builder()
                        .success(true)
                        .message("Search completed successfully")
                        .data(response)
                        .build()
        );
    }
}