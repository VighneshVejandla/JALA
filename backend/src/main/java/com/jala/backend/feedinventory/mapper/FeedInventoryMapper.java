package com.jala.backend.feedinventory.mapper;

import com.jala.backend.feedinventory.dto.response.FeedInventoryResponse;
import com.jala.backend.feedinventory.entity.FeedInventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedInventoryMapper {

    @Mapping(source = "site.id", target = "siteId")
    @Mapping(source = "site.siteCode", target = "siteCode")
    @Mapping(source = "site.siteName", target = "siteName")
    FeedInventoryResponse toResponse(FeedInventory entity);
}