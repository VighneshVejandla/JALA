package com.jala.backend.user.service;

import com.jala.backend.common.constants.MessageConstants;
import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.role.entity.Role;
import com.jala.backend.role.repository.RoleRepository;
import com.jala.backend.siteaccess.service.SiteAssignmentService;
import com.jala.backend.user.dto.request.UpdateUserRequest;
import com.jala.backend.user.dto.request.CreateUserRequest;
import com.jala.backend.user.dto.request.ResetPasswordRequest;
import com.jala.backend.user.dto.response.UserResponse;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.mapper.UserMapper;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final SiteAssignmentService siteAssignmentService;

    private void validateFirstAdminCreation(CreateUserRequest request) {

        if (!adminExists()) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() ->
                            new ResourceNotFoundException("ADMIN role not found"));

            if (!adminRole.getId().equals(request.getRoleId())) {
                throw new BadRequestException(
                        "The first user created must have the ADMIN role."
                );
            }
        }
    }

    @Override
    @Transactional
    public void resetPassword(UUID id, ResetPasswordRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset for user {}", id);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        validateFirstAdminCreation(request);

        log.info("Creating user {}", request.getEmployeeCode());

        if (userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BadRequestException("Employee Code already exists");
        }

        if (request.getEmail() != null &&
                !request.getEmail().isBlank() &&
                userRepository.existsByEmail(request.getEmail())) {

            throw new BadRequestException("Email already exists");
        }

        if (request.getPhone() != null &&
                !request.getPhone().isBlank() &&
                userRepository.existsByPhone(request.getPhone())) {

            throw new BadRequestException("Phone already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.ROLE_NOT_FOUND));

        User user = userMapper.toEntity(request);

        user.setRole(role);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        log.info("User {} created successfully", savedUser.getEmployeeCode());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse patchUser(UUID id, UpdateUserRequest request) {

        User user = getUserOrThrow(id);

        if (request.getRoleId() != null) {

            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Role not found"));

            if (!role.getId().equals(user.getRole().getId())) {
                // Role changed: revoke outstanding tokens so the new
                // authorities take effect immediately.
                revokeTokens(user);
            }

            user.setRole(role);
        }

        if (request.getFullName() != null &&
                !request.getFullName().isBlank()) {

            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null &&
                !request.getEmail().isBlank()) {

            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {

                throw new BadRequestException("Email already exists");
            }

            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null &&
                !request.getPhone().isBlank()) {

            if (!request.getPhone().equals(user.getPhone()) &&
                    userRepository.existsByPhone(request.getPhone())) {

                throw new BadRequestException("Phone already exists");
            }

            user.setPhone(request.getPhone());
        }

        if (request.getIsActive() != null) {

            if (Boolean.FALSE.equals(request.getIsActive()) &&
                    Boolean.TRUE.equals(user.getIsActive())) {
                revokeTokens(user);
            }

            user.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(user);

        log.info("User {} updated successfully",
                updatedUser.getEmployeeCode());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        user.setIsActive(true);

        userRepository.save(user);

        log.info("User {} activated", user.getEmployeeCode());
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        user.setIsActive(false);

        revokeTokens(user);

        userRepository.save(user);

        log.info("User {} deactivated", user.getEmployeeCode());
    }

    /**
     * Invalidates every JWT issued before this call by moving the user's
     * token version past the version embedded in those tokens.
     */
    private void revokeTokens(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    private User getUserOrThrow(UUID id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));
    }

    @Override
    public boolean adminExists() {
        return userRepository.existsByRole_Name("ADMIN");
    }

    @Override
    public void assignSite(UUID userId, UUID siteId) {
        siteAssignmentService.assignSite(userId, siteId);
    }

    @Override
    public void unassignSite(UUID userId, UUID siteId) {
        siteAssignmentService.unassignSite(userId, siteId);
    }

    @Override
    public List<UUID> getAssignedSiteIds(UUID userId) {
        return siteAssignmentService.getAssignedSiteIds(userId);
    }

}