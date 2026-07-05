package com.jala.backend.auth.service;

import com.jala.backend.auth.dto.LoginRequest;
import com.jala.backend.auth.dto.LoginResponse;
import com.jala.backend.auth.jwt.JwtService;
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

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String employeeCode) {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmployeeCode(),
                                request.getPassword()
                        )
                );

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