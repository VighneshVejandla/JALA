package com.jala.backend.feedschedule.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class FeedScheduleResponse {

    private UUID id;

    private UUID pondCycleId;

    private Integer sessionNumber;

    private LocalTime feedingTime;

    private Boolean isActive;
}