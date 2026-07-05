package com.jala.backend.site.service;

import com.jala.backend.site.dto.request.CreateSiteRequest;
import com.jala.backend.site.dto.request.UpdateSiteRequest;
import com.jala.backend.site.dto.response.SiteResponse;

import java.util.List;
import java.util.UUID;

public interface SiteService {

    SiteResponse createSite(CreateSiteRequest request);

    List<SiteResponse> getAllSites();

    SiteResponse getSiteById(UUID id);

    SiteResponse patchSite(UUID id, UpdateSiteRequest request);

    void activateSite(UUID id);

    void deactivateSite(UUID id);
}