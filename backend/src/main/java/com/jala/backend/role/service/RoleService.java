package com.jala.backend.role.service;

import com.jala.backend.role.dto.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(UUID id);

}