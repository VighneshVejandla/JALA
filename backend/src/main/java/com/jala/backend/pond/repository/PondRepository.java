package com.jala.backend.pond.repository;

import com.jala.backend.pond.entity.Pond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PondRepository extends JpaRepository<Pond, UUID> {

    boolean existsBySiteIdAndPondCode(UUID siteId, String pondCode);

    List<Pond> findBySiteIdOrderByPondCode(UUID siteId);

    long countBySiteId(
            UUID siteId);

    @Query("""
        SELECT p
        FROM Pond p
        WHERE LOWER(p.pondCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.pondName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.pondCode
        """)
    List<Pond> search(
            String keyword);
}