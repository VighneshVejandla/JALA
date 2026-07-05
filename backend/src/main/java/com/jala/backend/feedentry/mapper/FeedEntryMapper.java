package com.jala.backend.feedentry.mapper;

import com.jala.backend.feedentry.dto.request.CreateFeedEntryRequest;
import com.jala.backend.feedentry.dto.response.FeedEntryResponse;
import com.jala.backend.feedentry.entity.FeedEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedEntryMapper {

    @Mapping(target = "pondCycle", ignore = true)
    @Mapping(target = "feedSchedule", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    FeedEntry toEntity(CreateFeedEntryRequest request);

    @Mapping(source = "pondCycle.id", target = "pondCycleId")
    @Mapping(source = "feedSchedule.id", target = "feedScheduleId")
    @Mapping(source = "feedSchedule.sessionNumber", target = "sessionNumber")
    @Mapping(expression = "java(entity.getFeedSize().getCode())", target = "feedSize")
    @Mapping(expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getEmployeeCode() : null)", target = "createdBy")
    FeedEntryResponse toResponse(FeedEntry entity);
}