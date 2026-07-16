package com.jala.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Partial update: every field is optional; only format constraints apply.
 */
@Data
public class UpdateUserRequest {

    private UUID roleId;

    @Size(max = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    private Boolean isActive;
}
