package com.jala.backend.pond.repository;

import com.jala.backend.pond.entity.Pond;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PondRepository extends JpaRepository<Pond, UUID> {

    boolean existsBySiteIdAndPondCode(UUID siteId, String pondCode);

    List<Pond> findBySiteIdOrderByPondCode(UUID siteId);
}