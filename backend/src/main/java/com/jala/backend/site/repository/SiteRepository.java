package com.jala.backend.site.repository;

import com.jala.backend.site.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    boolean existsBySiteCode(String siteCode);

    Optional<Site> findBySiteCode(String siteCode);

    Optional<Site> findById(UUID id);
}