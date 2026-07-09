package com.jala.backend.history.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PondTimelineResponse {

    private UUID pondId;

    private String pondCode;

    private String pondName;

    private List<PondTimelineItemResponse> timeline;
}