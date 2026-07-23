package com.jala.backend.user.service;

import com.jala.backend.user.dto.request.UpdateUserRequest;
import com.jala.backend.user.dto.request.CreateUserRequest;
import com.jala.backend.user.dto.request.ResetPasswordRequest;
import com.jala.backend.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    void resetPassword(UUID id, ResetPasswordRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(UUID id);

    UserResponse patchUser(UUID id, UpdateUserRequest request);

    void activateUser(UUID id);

    void deactivateUser(UUID id);

    boolean adminExists();

    void assignSite(UUID userId, UUID siteId);

    void unassignSite(UUID userId, UUID siteId);

    List<UUID> getAssignedSiteIds(UUID userId);

}