package com.jala.backend.search.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GlobalSearchResponse {

    private List<SearchResultResponse> users;

    private List<SearchResultResponse> sites;

    private List<SearchResultResponse> ponds;

    private List<SearchResultResponse> feedEntries;

    private List<SearchResultResponse> medicineEntries;

    private List<SearchResultResponse> harvests;

    private List<SearchResultResponse> notifications;
}