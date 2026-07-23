package com.jala.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Self-service profile fields a signed-in user may update. */
@Data
public class UpdateProfileRequest {

    @Size(max = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;
}
