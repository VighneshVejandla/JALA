package com.jala.backend.siteaccess.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.entity.UserSite;
import com.jala.backend.siteaccess.repository.UserSiteRepository;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Admin-facing management of the {@code user_sites} assignments that
 * {@link SiteAccessService} enforces at read/write time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteAssignmentService {

    private final UserSiteRepository userSiteRepository;

    private final UserRepository userRepository;

    private final SiteRepository siteRepository;

    @Transactional
    public void assignSite(UUID userId, UUID siteId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site not found.");
        }

        if (userSiteRepository.existsByUserIdAndSiteId(userId, siteId)) {
            return;
        }

        userSiteRepository.save(
                UserSite.builder()
                        .userId(userId)
                        .siteId(siteId)
                        .build());

        log.info("Site {} assigned to user {}", siteId, userId);
    }

    @Transactional
    public void unassignSite(UUID userId, UUID siteId) {

        userSiteRepository.deleteByUserIdAndSiteId(userId, siteId);

        log.info("Site {} unassigned from user {}", siteId, userId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getAssignedSiteIds(UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return userSiteRepository.findSiteIdsByUserId(userId);
    }
}
