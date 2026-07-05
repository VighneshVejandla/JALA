package com.jala.backend.feedschedule.mapper;

import com.jala.backend.feedschedule.dto.request.CreateFeedScheduleRequest;
import com.jala.backend.feedschedule.dto.response.FeedScheduleResponse;
import com.jala.backend.feedschedule.entity.FeedSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedScheduleMapper {

    @Mapping(target = "pondCycle", ignore = true)
    @Mapping(target = "sessionNumber", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    FeedSchedule toEntity(CreateFeedScheduleRequest request);

    @Mapping(source = "pondCycle.id", target = "pondCycleId")
    FeedScheduleResponse toResponse(FeedSchedule entity);
}