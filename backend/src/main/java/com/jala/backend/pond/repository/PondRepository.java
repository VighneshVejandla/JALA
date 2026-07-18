package com.jala.backend.pond.repository;

import com.jala.backend.pond.entity.Pond;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PondRepository extends JpaRepository<Pond, UUID> {

    boolean existsBySiteIdAndPondCode(UUID siteId, String pondCode);

    @Query("SELECT p.site.id FROM Pond p WHERE p.id = :pondId")
    Optional<UUID> findSiteIdByPondId(UUID pondId);

    List<Pond> findBySiteIdOrderByPondCode(UUID siteId);

    List<Pond> findBySiteIdInOrderByPondCode(Collection<UUID> siteIds);

    long countBySiteId(
            UUID siteId);

    @Query("""
        SELECT p
        FROM Pond p
        JOIN FETCH p.site
        WHERE LOWER(p.pondCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.pondName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.pondCode
        """)
    List<Pond> search(
            String keyword,
            Pageable pageable);
}