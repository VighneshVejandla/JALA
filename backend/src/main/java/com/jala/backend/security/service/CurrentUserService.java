package com.jala.backend.security.service;

import com.jala.backend.common.constants.RoleConstants;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Single place to resolve the authenticated {@link User}, replacing the
 * SecurityContext + repository lookup previously copy-pasted (and left
 * null-unsafe) across the service layer.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }

        return userRepository.findByEmployeeCode(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Authenticated user no longer exists"));
    }

    /** True when the current user holds an unrestricted role. */
    public boolean isUnrestricted() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_" + RoleConstants.ADMIN)
                        || a.getAuthority().equals("ROLE_" + RoleConstants.MANAGER));
    }
}
