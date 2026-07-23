package com.jala.backend.user.service;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.role.entity.Role;
import com.jala.backend.role.repository.RoleRepository;
import com.jala.backend.siteaccess.service.SiteAssignmentService;
import com.jala.backend.user.dto.request.CreateUserRequest;
import com.jala.backend.user.dto.request.UpdateUserRequest;
import com.jala.backend.user.dto.response.UserResponse;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.mapper.UserMapper;
import com.jala.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SiteAssignmentService siteAssignmentService;

    @InjectMocks
    private UserServiceImpl userService;

    private Role adminRole;
    private Role workerRole;
    private User user;

    @BeforeEach
    void setUp() {

        adminRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .build();

        workerRole = Role.builder()
                .id(UUID.randomUUID())
                .name("WORKER")
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .role(workerRole)
                .employeeCode("EMP-001")
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("9999999999")
                .passwordHash("hash")
                .isActive(true)
                .tokenVersion(0)
                .build();
    }

    private CreateUserRequest createRequest(UUID roleId) {

        CreateUserRequest request = new CreateUserRequest();
        request.setRoleId(roleId);
        request.setEmployeeCode("EMP-001");
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPhone("9999999999");
        request.setPassword("secret-password1");
        return request;
    }

    private UserResponse response() {
        return UserResponse.builder()
                .id(user.getId())
                .employeeCode("EMP-001")
                .build();
    }

    @Nested
    class CreateUser {

        @Test
        @DisplayName("happy path encodes password, sets role and activates the user")
        void createUser_success() {

            CreateUserRequest request = createRequest(workerRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("9999999999")).thenReturn(false);
            when(roleRepository.findById(workerRole.getId()))
                    .thenReturn(Optional.of(workerRole));

            User mapped = User.builder()
                    .id(UUID.randomUUID())
                    .employeeCode("EMP-001")
                    .fullName("Jane Doe")
                    .tokenVersion(0)
                    .build();

            when(userMapper.toEntity(request)).thenReturn(mapped);
            when(passwordEncoder.encode("secret-password1")).thenReturn("encoded");
            when(userRepository.save(mapped)).thenAnswer(inv -> inv.getArgument(0));

            UserResponse expected = response();
            when(userMapper.toResponse(mapped)).thenReturn(expected);

            UserResponse actual = userService.createUser(request);

            assertThat(actual).isSameAs(expected);
            assertThat(mapped.getPasswordHash()).isEqualTo("encoded");
            assertThat(mapped.getRole()).isSameAs(workerRole);
            assertThat(mapped.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("null email and phone skip the duplicate checks")
        void createUser_nullEmailAndPhone_skipsDuplicateChecks() {

            CreateUserRequest request = createRequest(workerRole.getId());
            request.setEmail(null);
            request.setPhone(null);

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(roleRepository.findById(workerRole.getId()))
                    .thenReturn(Optional.of(workerRole));

            User mapped = User.builder().id(UUID.randomUUID()).build();
            when(userMapper.toEntity(request)).thenReturn(mapped);
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(mapped)).thenReturn(mapped);
            when(userMapper.toResponse(mapped)).thenReturn(response());

            userService.createUser(request);

            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).existsByPhone(any());
        }

        @Test
        @DisplayName("duplicate employee code is rejected")
        void createUser_duplicateEmployeeCode() {

            CreateUserRequest request = createRequest(workerRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Employee Code already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("duplicate email is rejected")
        void createUser_duplicateEmail() {

            CreateUserRequest request = createRequest(workerRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already exists");
        }

        @Test
        @DisplayName("duplicate phone is rejected")
        void createUser_duplicatePhone() {

            CreateUserRequest request = createRequest(workerRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("9999999999")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Phone already exists");
        }

        @Test
        @DisplayName("missing role is rejected")
        void createUser_roleNotFound() {

            UUID roleId = UUID.randomUUID();
            CreateUserRequest request = createRequest(roleId);

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("9999999999")).thenReturn(false);
            when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Role not found");
        }

        @Test
        @DisplayName("first user must be ADMIN: non-admin role is rejected")
        void createUser_firstUser_nonAdminRole_rejected() {

            CreateUserRequest request = createRequest(workerRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(false);
            when(roleRepository.findByName("ADMIN"))
                    .thenReturn(Optional.of(adminRole));

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("The first user created must have the ADMIN role.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("first user with the ADMIN role id passes validation")
        void createUser_firstUser_adminRole_allowed() {

            CreateUserRequest request = createRequest(adminRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(false);
            when(roleRepository.findByName("ADMIN"))
                    .thenReturn(Optional.of(adminRole));
            when(userRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("9999999999")).thenReturn(false);
            when(roleRepository.findById(adminRole.getId()))
                    .thenReturn(Optional.of(adminRole));

            User mapped = User.builder().id(UUID.randomUUID()).build();
            when(userMapper.toEntity(request)).thenReturn(mapped);
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(mapped)).thenReturn(mapped);
            when(userMapper.toResponse(mapped)).thenReturn(response());

            UserResponse actual = userService.createUser(request);

            assertThat(actual).isNotNull();
            assertThat(mapped.getRole()).isSameAs(adminRole);
        }

        @Test
        @DisplayName("first user validation fails when the ADMIN role is missing")
        void createUser_firstUser_adminRoleMissing() {

            CreateUserRequest request = createRequest(workerRole.getId());

            when(userRepository.existsByRole_Name("ADMIN")).thenReturn(false);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("ADMIN role not found");
        }
    }

    @Nested
    class PatchUser {

        @Test
        @DisplayName("unknown user id is rejected")
        void patchUser_notFound() {

            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.patchUser(id, new UpdateUserRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("role change bumps the token version")
        void patchUser_roleChanged_bumpsTokenVersion() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setRoleId(adminRole.getId());

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(roleRepository.findById(adminRole.getId()))
                    .thenReturn(Optional.of(adminRole));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getRole()).isSameAs(adminRole);
            assertThat(user.getTokenVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("same role does not bump the token version")
        void patchUser_sameRole_noTokenBump() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setRoleId(workerRole.getId());

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(roleRepository.findById(workerRole.getId()))
                    .thenReturn(Optional.of(workerRole));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getRole()).isSameAs(workerRole);
            assertThat(user.getTokenVersion()).isZero();
        }

        @Test
        @DisplayName("unknown role id is rejected")
        void patchUser_roleNotFound() {

            UUID roleId = UUID.randomUUID();
            UpdateUserRequest request = new UpdateUserRequest();
            request.setRoleId(roleId);

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.patchUser(user.getId(), request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Role not found");
        }

        @Test
        @DisplayName("full name is updated when provided")
        void patchUser_fullName() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setFullName("New Name");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getFullName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("new email owned by someone else is rejected")
        void patchUser_duplicateEmail() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setEmail("taken@example.com");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("taken@example.com"))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    userService.patchUser(user.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already exists");
        }

        @Test
        @DisplayName("unchanged email skips the duplicate check")
        void patchUser_sameEmail_skipsDuplicateCheck() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setEmail("jane@example.com");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            verify(userRepository, never()).existsByEmail(any());
            assertThat(user.getEmail()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("available new email is applied")
        void patchUser_newEmail_applied() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setEmail("new@example.com");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@example.com"))
                    .thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getEmail()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("new phone owned by someone else is rejected")
        void patchUser_duplicatePhone() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setPhone("8888888888");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.existsByPhone("8888888888")).thenReturn(true);

            assertThatThrownBy(() ->
                    userService.patchUser(user.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Phone already exists");
        }

        @Test
        @DisplayName("available new phone is applied")
        void patchUser_newPhone_applied() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setPhone("8888888888");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.existsByPhone("8888888888")).thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getPhone()).isEqualTo("8888888888");
        }

        @Test
        @DisplayName("deactivating an active user bumps the token version")
        void patchUser_deactivate_bumpsTokenVersion() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setIsActive(false);

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getIsActive()).isFalse();
            assertThat(user.getTokenVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("setting isActive true does not bump the token version")
        void patchUser_activate_noTokenBump() {

            UpdateUserRequest request = new UpdateUserRequest();
            request.setIsActive(true);

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getIsActive()).isTrue();
            assertThat(user.getTokenVersion()).isZero();
        }

        @Test
        @DisplayName("all-null request changes nothing but still saves")
        void patchUser_allNull_noChanges() {

            UpdateUserRequest request = new UpdateUserRequest();

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(response());

            userService.patchUser(user.getId(), request);

            assertThat(user.getFullName()).isEqualTo("Jane Doe");
            assertThat(user.getEmail()).isEqualTo("jane@example.com");
            assertThat(user.getPhone()).isEqualTo("9999999999");
            assertThat(user.getIsActive()).isTrue();
            assertThat(user.getTokenVersion()).isZero();
            verify(userRepository).save(user);
        }
    }

    @Test
    @DisplayName("activateUser sets the user active")
    void activateUser_success() {

        user.setIsActive(false);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        userService.activateUser(user.getId());

        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getTokenVersion()).isZero();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("activateUser rejects unknown ids")
    void activateUser_notFound() {

        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("deactivateUser sets the user inactive and bumps the token version")
    void deactivateUser_success() {

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        userService.deactivateUser(user.getId());

        assertThat(user.getIsActive()).isFalse();
        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deactivateUser rejects unknown ids")
    void deactivateUser_notFound() {

        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("getAllUsers maps every user")
    void getAllUsers_mapsAll() {

        User other = User.builder()
                .id(UUID.randomUUID())
                .role(adminRole)
                .employeeCode("EMP-002")
                .fullName("John Doe")
                .passwordHash("hash")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user, other));
        when(userMapper.toResponse(any(User.class)))
                .thenAnswer(inv -> UserResponse.builder()
                        .id(((User) inv.getArgument(0)).getId())
                        .build());

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(UserResponse::getId)
                .containsExactly(user.getId(), other.getId());
    }

    @Test
    @DisplayName("getUserById returns the mapped user")
    void getUserById_found() {

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UserResponse expected = response();
        when(userMapper.toResponse(user)).thenReturn(expected);

        assertThat(userService.getUserById(user.getId()))
                .isSameAs(expected);
    }

    @Test
    @DisplayName("getUserById rejects unknown ids")
    void getUserById_notFound() {

        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("adminExists reflects the repository result")
    void adminExists_delegates() {

        when(userRepository.existsByRole_Name("ADMIN")).thenReturn(true);
        assertThat(userService.adminExists()).isTrue();

        when(userRepository.existsByRole_Name("ADMIN")).thenReturn(false);
        assertThat(userService.adminExists()).isFalse();
    }

    @Test
    @DisplayName("assignSite delegates to SiteAssignmentService")
    void assignSite_delegates() {

        UUID userId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        userService.assignSite(userId, siteId);

        verify(siteAssignmentService).assignSite(userId, siteId);
    }

    @Test
    @DisplayName("unassignSite delegates to SiteAssignmentService")
    void unassignSite_delegates() {

        UUID userId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        userService.unassignSite(userId, siteId);

        verify(siteAssignmentService).unassignSite(userId, siteId);
    }

    @Test
    @DisplayName("getAssignedSiteIds delegates to SiteAssignmentService")
    void getAssignedSiteIds_delegates() {

        UUID userId = UUID.randomUUID();
        List<UUID> siteIds = List.of(UUID.randomUUID());

        when(siteAssignmentService.getAssignedSiteIds(userId))
                .thenReturn(siteIds);

        assertThat(userService.getAssignedSiteIds(userId))
                .isSameAs(siteIds);
    }

    @Test
    @DisplayName("resetPassword re-encodes and saves")
    void resetPassword_success() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password-123")).thenReturn("new-hash");

        var req = new com.jala.backend.user.dto.request.ResetPasswordRequest();
        req.setNewPassword("new-password-123");

        userService.resetPassword(user.getId(), req);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("resetPassword rejects an unknown user")
    void resetPassword_notFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        var req = new com.jala.backend.user.dto.request.ResetPasswordRequest();
        req.setNewPassword("new-password-123");
        assertThatThrownBy(() -> userService.resetPassword(id, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
