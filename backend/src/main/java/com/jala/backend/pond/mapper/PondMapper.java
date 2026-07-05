package com.jala.backend.pond.mapper;

import com.jala.backend.pond.dto.request.CreatePondRequest;
import com.jala.backend.pond.dto.response.PondResponse;
import com.jala.backend.pond.entity.Pond;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PondMapper {

    @Mapping(target = "site", ignore = true)
    Pond toEntity(CreatePondRequest request);

    @Mapping(source = "site.id", target = "siteId")
    @Mapping(source = "site.siteName", target = "siteName")
    PondResponse toResponse(Pond pond);
}