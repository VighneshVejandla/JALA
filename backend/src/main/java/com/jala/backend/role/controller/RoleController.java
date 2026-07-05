package com.jala.backend.role.controller;

import com.jala.backend.common.constants.ApiConstants;
import com.jala.backend.role.dto.RoleResponse;
import com.jala.backend.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ROLE_BASE_URL)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public RoleResponse getRoleById(@PathVariable UUID id) {
        return roleService.getRoleById(id);
    }
}