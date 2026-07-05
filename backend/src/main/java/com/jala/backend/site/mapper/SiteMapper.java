package com.jala.backend.site.mapper;

import com.jala.backend.site.dto.request.CreateSiteRequest;
import com.jala.backend.site.dto.response.SiteResponse;
import com.jala.backend.site.entity.Site;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SiteMapper {

    Site toEntity(CreateSiteRequest request);

    SiteResponse toResponse(Site site);
}