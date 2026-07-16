package com.jala.backend.user.controller;

import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.security.BootstrapSecurity;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import com.jala.backend.user.dto.request.CreateUserRequest;
import com.jala.backend.user.dto.request.UpdateUserRequest;
import com.jala.backend.user.dto.response.UserResponse;
import com.jala.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(WebSecurityTestConfig.class)
class UserControllerTest extends WebSliceTestBase {

    @MockitoBean
    private UserService userService;

    /**
     * The create-user rule references the {@code bootstrapSecurity} bean by
     * name in SpEL; register a mock under that exact name so the expression
     * evaluates in the slice (mock returns {@code false} by default).
     */
    @MockitoBean(name = "bootstrapSecurity")
    private BootstrapSecurity bootstrapSecurity;

    private static UserResponse userResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP-001")
                .fullName("Alice Admin")
                .email("alice@example.com")
                .role("ADMIN")
                .isActive(true)
                .build();
    }

    private CreateUserRequest validCreateRequest() {

        CreateUserRequest request = new CreateUserRequest();
        request.setRoleId(UUID.randomUUID());
        request.setEmployeeCode("EMP-001");
        request.setFullName("Alice Admin");
        request.setEmail("alice@example.com");
        request.setPassword("longpassword123");
        return request;
    }

    // ---- POST /api/v1/users -------------------------------------------

    @Test
    @DisplayName("anonymous create user is rejected with 401")
    void createUser_anonymous_unauthorized() throws Exception {

        String body = objectMapper.writeValueAsString(validCreateRequest());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("create user requires ADMIN when an admin already exists")
    void createUser_worker_forbidden() throws Exception {

        given(bootstrapSecurity.canCreateFirstAdmin()).willReturn(false);

        String body = objectMapper.writeValueAsString(validCreateRequest());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("bootstrap branch lets a non-admin create the first admin")
    void createUser_bootstrap_created() throws Exception {

        given(bootstrapSecurity.canCreateFirstAdmin()).willReturn(true);
        given(userService.createUser(any(CreateUserRequest.class)))
                .willReturn(userResponse());

        String body = objectMapper.writeValueAsString(validCreateRequest());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin creates user and gets 201 envelope")
    void createUser_admin_created() throws Exception {

        given(userService.createUser(any(CreateUserRequest.class)))
                .willReturn(userResponse());

        String body = objectMapper.writeValueAsString(validCreateRequest());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("User created successfully"))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("empty create body returns 400 validation envelope")
    void createUser_invalidBody_badRequest() throws Exception {

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("short password fails create validation with field error")
    void createUser_shortPassword_badRequest() throws Exception {

        CreateUserRequest request = validCreateRequest();
        request.setPassword("short1");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.password").exists());
    }

    // ---- GET /api/v1/users --------------------------------------------

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("manager can list users")
    void getAllUsers_manager_ok() throws Exception {

        given(userService.getAllUsers()).willReturn(List.of(userResponse()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Users fetched successfully"))
                .andExpect(jsonPath("$.data[0].employeeCode").value("EMP-001"));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker cannot list users")
    void getAllUsers_worker_forbidden() throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    // ---- GET /api/v1/users/{id} ---------------------------------------

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("manager can fetch a user by id")
    void getUserById_manager_ok() throws Exception {

        UUID id = UUID.randomUUID();
        given(userService.getUserById(id)).willReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("User fetched successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("unknown user id returns 404 envelope")
    void getUserById_notFound() throws Exception {

        UUID id = UUID.randomUUID();
        given(userService.getUserById(id))
                .willThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("non-UUID user id returns 400")
    void getUserById_invalidUuid_badRequest() throws Exception {

        mockMvc.perform(get("/api/v1/users/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'id'"));
    }

    // ---- PATCH /api/v1/users/{id} -------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin patches a user and gets 200 envelope")
    void patchUser_admin_ok() throws Exception {

        UUID id = UUID.randomUUID();
        given(userService.patchUser(eq(id), any(UpdateUserRequest.class)))
                .willReturn(userResponse());

        mockMvc.perform(patch("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("User updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("patch with invalid email returns 400 validation envelope")
    void patchUser_invalidEmail_badRequest() throws Exception {

        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("manager cannot patch a user")
    void patchUser_manager_forbidden() throws Exception {

        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Name\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("service BadRequestException surfaces as 400 envelope")
    void patchUser_serviceRejects_badRequest() throws Exception {

        given(userService.patchUser(any(UUID.class),
                any(UpdateUserRequest.class)))
                .willThrow(new BadRequestException("Role does not exist"));

        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Name\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Role does not exist"));
    }

    // ---- activate / deactivate ----------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin activates a user")
    void activateUser_admin_ok() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/users/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("User activated successfully"));

        then(userService).should().activateUser(id);
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker cannot activate a user")
    void activateUser_worker_forbidden() throws Exception {

        mockMvc.perform(patch("/api/v1/users/{id}/activate",
                        UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin deactivates a user")
    void deactivateUser_admin_ok() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("User deactivated successfully"));

        then(userService).should().deactivateUser(id);
    }

    // ---- site assignment ----------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin assigns a site and gets 201")
    void assignSite_admin_created() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/users/{id}/sites/{siteId}",
                        userId, siteId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Site assigned successfully"));

        then(userService).should().assignSite(userId, siteId);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("site assignment is ADMIN-only")
    void assignSite_manager_forbidden() throws Exception {

        mockMvc.perform(post("/api/v1/users/{id}/sites/{siteId}",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin unassigns a site and gets 200")
    void unassignSite_admin_ok() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}/sites/{siteId}",
                        userId, siteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Site unassigned successfully"));

        then(userService).should().unassignSite(userId, siteId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("unassign failure surfaces as 404 envelope")
    void unassignSite_notFound() throws Exception {

        willThrow(new ResourceNotFoundException("Assignment not found"))
                .given(userService)
                .unassignSite(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/v1/users/{id}/sites/{siteId}",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Assignment not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("admin lists a user's assigned site ids")
    void getAssignedSites_admin_ok() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        given(userService.getAssignedSiteIds(userId))
                .willReturn(List.of(siteId));

        mockMvc.perform(get("/api/v1/users/{id}/sites", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Assigned sites fetched successfully"))
                .andExpect(jsonPath("$.data[0]").value(siteId.toString()));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker cannot list a user's assigned sites")
    void getAssignedSites_worker_forbidden() throws Exception {

        mockMvc.perform(get("/api/v1/users/{id}/sites", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
