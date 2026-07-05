package com.jala.backend.feeddelivery.mapper;

import com.jala.backend.feeddelivery.dto.request.CreateFeedDeliveryRequest;
import com.jala.backend.feeddelivery.dto.response.FeedDeliveryResponse;
import com.jala.backend.feeddelivery.entity.FeedDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedDeliveryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deliveredBy", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "restoredBy", ignore = true)
    @Mapping(target = "restoredAt", ignore = true)
    @Mapping(target = "restorationReason", ignore = true)
    FeedDelivery toEntity(CreateFeedDeliveryRequest request);

    @Mapping(source = "deliveredBy.employeeCode", target = "deliveredBy")
    @Mapping(expression = "java(entity.getStatus().name())", target = "status")
    FeedDeliveryResponse toResponse(FeedDelivery entity);
}