package com.jala.backend.feedschedule.dto.request;

import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateFeedScheduleRequest {

    private LocalTime feedingTime;

    private Boolean isActive;
}