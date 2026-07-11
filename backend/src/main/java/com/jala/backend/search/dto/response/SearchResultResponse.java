package com.jala.backend.search.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SearchResultResponse {

    private UUID id;

    private String type;

    private String title;

    private String subtitle;
}