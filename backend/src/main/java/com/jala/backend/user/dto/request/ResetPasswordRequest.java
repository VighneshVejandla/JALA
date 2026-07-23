package com.jala.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Admin-initiated password reset for a user. */
@Data
public class ResetPasswordRequest {

    @NotBlank
    @Size(min = 12, max = 128)
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain a letter and a digit")
    private String newPassword;
}
