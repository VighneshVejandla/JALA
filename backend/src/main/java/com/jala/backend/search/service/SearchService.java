package com.jala.backend.search.service;

import com.jala.backend.search.dto.response.GlobalSearchResponse;

public interface SearchService {

    GlobalSearchResponse search(
            String keyword);
}