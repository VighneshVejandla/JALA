package com.jala.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateUserRequest {

    @NotNull
    private UUID roleId;

    @NotBlank
    @Size(max = 20)
    private String employeeCode;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(min = 12, max = 128,
            message = "Password must be between 12 and 128 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$",
            message = "Password must contain at least one letter and one digit")
    private String password;

}
