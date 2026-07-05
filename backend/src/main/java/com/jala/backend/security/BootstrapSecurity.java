package com.jala.backend.security;

import com.jala.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("bootstrapSecurity")
@RequiredArgsConstructor
public class BootstrapSecurity {

    private final UserService userService;

    public boolean canCreateFirstAdmin() {
        return !userService.adminExists();
    }
}