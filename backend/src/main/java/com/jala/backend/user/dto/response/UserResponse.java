package com.jala.backend.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;

    private String employeeCode;

    private String fullName;

    private String email;

    private String phone;

    private String role;

    private Boolean isActive;
}