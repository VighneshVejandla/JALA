package com.jala.backend.auth.service;

import com.jala.backend.auth.dto.ChangePasswordRequest;
import com.jala.backend.auth.dto.LoginRequest;
import com.jala.backend.auth.dto.LoginResponse;
import com.jala.backend.auth.dto.UpdateProfileRequest;
import com.jala.backend.user.dto.response.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(String employeeCode);

    UserResponse updateProfile(String employeeCode, UpdateProfileRequest request);

    void changePassword(String employeeCode, ChangePasswordRequest request);

}