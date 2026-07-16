package com.jala.backend.role.service;

import com.jala.backend.role.dto.RoleResponse;
import com.jala.backend.role.entity.Role;
import com.jala.backend.role.mapper.RoleMapper;
import com.jala.backend.role.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private static Role role(String name) {
        return Role.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(name + " role")
                .build();
    }

    @Test
    @DisplayName("getAllRoles maps every role")
    void getAllRoles_mapsAll() {

        Role admin = role("ADMIN");
        Role worker = role("WORKER");

        when(roleRepository.findAll()).thenReturn(List.of(admin, worker));
        when(roleMapper.toResponse(any(Role.class)))
                .thenAnswer(inv -> {
                    Role r = inv.getArgument(0);
                    return RoleResponse.builder()
                            .id(r.getId())
                            .name(r.getName())
                            .description(r.getDescription())
                            .build();
                });

        List<RoleResponse> responses = roleService.getAllRoles();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(RoleResponse::getName)
                .containsExactly("ADMIN", "WORKER");
    }

    @Test
    @DisplayName("getAllRoles returns an empty list when no roles exist")
    void getAllRoles_empty() {

        when(roleRepository.findAll()).thenReturn(List.of());

        assertThat(roleService.getAllRoles()).isEmpty();
    }

    @Test
    @DisplayName("getRoleById returns the mapped role")
    void getRoleById_found() {

        Role admin = role("ADMIN");

        RoleResponse expected = RoleResponse.builder()
                .id(admin.getId())
                .name("ADMIN")
                .build();

        when(roleRepository.findById(admin.getId()))
                .thenReturn(Optional.of(admin));
        when(roleMapper.toResponse(admin)).thenReturn(expected);

        assertThat(roleService.getRoleById(admin.getId()))
                .isSameAs(expected);
    }

    @Test
    @DisplayName("getRoleById throws for unknown ids")
    void getRoleById_notFound() {

        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        // Current behavior: a bare RuntimeException, not
        // ResourceNotFoundException like the other services.
        assertThatThrownBy(() -> roleService.getRoleById(id))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Role not found");

        verifyNoInteractions(roleMapper);
    }
}
