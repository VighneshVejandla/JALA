package com.jala.backend.siteaccess.service;

import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.repository.PondCycleRepository;
import com.jala.backend.security.service.CurrentUserService;
import com.jala.backend.siteaccess.repository.UserSiteRepository;
import com.jala.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Object-level authorization: ADMIN and MANAGER see everything;
 * SUPERVISOR, WORKER and DRIVER only the sites assigned to them
 * via {@code user_sites}.
 */
@Service
@RequiredArgsConstructor
public class SiteAccessService {

    private static final String SITE_ACCESS_DENIED =
            "You do not have access to this site";

    private final CurrentUserService currentUserService;
    private final UserSiteRepository userSiteRepository;
    private final PondRepository pondRepository;
    private final PondCycleRepository pondCycleRepository;

    @Transactional(readOnly = true)
    public void checkSiteAccess(UUID siteId) {

        if (currentUserService.isUnrestricted()) {
            return;
        }

        User user = currentUserService.getCurrentUser();

        if (!userSiteRepository.existsByUserIdAndSiteId(user.getId(), siteId)) {
            throw new AccessDeniedException(SITE_ACCESS_DENIED);
        }
    }

    @Transactional(readOnly = true)
    public void checkPondAccess(UUID pondId) {

        if (currentUserService.isUnrestricted()) {
            return;
        }

        UUID siteId = pondRepository.findSiteIdByPondId(pondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pond not found."));

        checkSiteAccess(siteId);
    }

    @Transactional(readOnly = true)
    public void checkPondCycleAccess(UUID pondCycleId) {

        if (currentUserService.isUnrestricted()) {
            return;
        }

        UUID siteId = pondCycleRepository.findSiteIdByCycleId(pondCycleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pond cycle not found."));

        checkSiteAccess(siteId);
    }

    /**
     * Site ids the current user may read, or {@code null} for
     * unrestricted roles (meaning: no filter).
     */
    @Transactional(readOnly = true)
    public List<UUID> accessibleSiteIds() {

        if (currentUserService.isUnrestricted()) {
            return null;
        }

        User user = currentUserService.getCurrentUser();

        return userSiteRepository.findSiteIdsByUserId(user.getId());
    }
}
