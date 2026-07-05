package com.jala.backend.feeddelivery.mapper;

import com.jala.backend.feeddelivery.dto.request.AddSiteDeliveryRequest;
import com.jala.backend.feeddelivery.dto.response.SiteDeliveryResponse;
import com.jala.backend.feeddelivery.entity.SiteDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SiteDeliveryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "feedDelivery", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "bagWeightKg", ignore = true)
    @Mapping(target = "totalKg", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "restoredBy", ignore = true)
    @Mapping(target = "restoredAt", ignore = true)
    @Mapping(target = "restorationReason", ignore = true)
    SiteDelivery toEntity(AddSiteDeliveryRequest request);

    @Mapping(source = "site.id", target = "siteId")
    @Mapping(source = "site.siteCode", target = "siteCode")
    @Mapping(source = "site.siteName", target = "siteName")
    @Mapping(expression = "java(entity.getStatus().name())", target = "status")
    SiteDeliveryResponse toResponse(SiteDelivery entity);
}