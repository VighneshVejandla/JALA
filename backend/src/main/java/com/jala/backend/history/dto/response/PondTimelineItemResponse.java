package com.jala.backend.history.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PondTimelineItemResponse {

    private LocalDateTime eventTime;

    private String eventType;

    private String title;

    private String description;

    private Integer cycleNumber;

    private UUID referenceId;

    private String referenceType;
}