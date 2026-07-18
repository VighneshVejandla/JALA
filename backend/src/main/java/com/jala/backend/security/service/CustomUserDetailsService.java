package com.jala.backend.security.service;

import com.jala.backend.security.model.CustomUserDetails;
import com.jala.backend.user.entity.User;
import com.jala.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String employeeCode)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with employee code: " + employeeCode));

        return new CustomUserDetails(user);
    }
}