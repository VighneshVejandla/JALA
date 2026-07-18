package com.jala.backend.siteaccess.repository;

import com.jala.backend.siteaccess.entity.UserSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserSiteRepository extends JpaRepository<UserSite, UUID> {

    boolean existsByUserIdAndSiteId(UUID userId, UUID siteId);

    void deleteByUserIdAndSiteId(UUID userId, UUID siteId);

    @Query("SELECT us.siteId FROM UserSite us WHERE us.userId = :userId")
    List<UUID> findSiteIdsByUserId(UUID userId);
}
