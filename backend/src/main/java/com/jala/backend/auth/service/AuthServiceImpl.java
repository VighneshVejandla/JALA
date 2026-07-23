package com.jala.backend.auth.service;

import com.jala.backend.auth.dto.ChangePasswordRequest;
import com.jala.backend.auth.dto.LoginRequest;
import com.jala.backend.auth.dto.LoginResponse;
import com.jala.backend.auth.dto.UpdateProfileRequest;
import com.jala.backend.auth.jwt.JwtService;
import com.jala.backend.common.exception.BadRequestException;
import com.jala.backend.common.exception.ResourceNotFoundException;
import com.jala.backend.config.JwtProperties;
import com.jala.backend.security.model.CustomUserDetails;
import com.jala.backend.user.dto.response.UserResponse;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.mapper.UserMapper;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final LoginAttemptService loginAttemptService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String employeeCode) {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(
            String employeeCode, UpdateProfileRequest request) {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(
            String employeeCode, ChangePasswordRequest request) {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        loginAttemptService.checkNotLocked(request.getEmployeeCode());

        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmployeeCode(),
                                    request.getPassword()
                            )
                    );
        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(request.getEmployeeCode());
            throw e;
        }

        loginAttemptService.reset(request.getEmployeeCode());

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .employeeCode(user.getUser().getEmployeeCode())
                .fullName(user.getUser().getFullName())
                .role(user.getUser().getRole().getName())
                .build();
    }

}