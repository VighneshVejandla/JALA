package com.jala.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String employeeCode;

    @NotBlank
    private String password;

}