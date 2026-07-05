package com.jala.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserRequest {

    @NotNull
    private UUID roleId;

    @NotBlank
    private String fullName;

    private String email;

    private String phone;

    private Boolean isActive;
}