package com.jala.backend.auth.service;

import com.jala.backend.auth.dto.LoginRequest;
import com.jala.backend.auth.dto.LoginResponse;
import com.jala.backend.user.dto.response.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(String employeeCode);

}