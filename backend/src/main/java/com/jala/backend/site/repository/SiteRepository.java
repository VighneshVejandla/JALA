package com.jala.backend.site.repository;

import com.jala.backend.site.entity.Site;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    boolean existsBySiteCode(String siteCode);

    Optional<Site> findBySiteCode(String siteCode);

    Optional<Site> findById(UUID id);

    List<Site> findByIdIn(Collection<UUID> ids);

    @Query("""
        SELECT s
        FROM Site s
        WHERE LOWER(s.siteCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.siteName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY s.siteCode
        """)
    List<Site> search(
            String keyword,
            Pageable pageable);
}