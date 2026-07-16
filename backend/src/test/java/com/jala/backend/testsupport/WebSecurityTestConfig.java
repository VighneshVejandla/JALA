package com.jala.backend.testsupport;

import com.jala.backend.common.exception.GlobalExceptionHandler;
import com.jala.backend.security.CustomAccessDeniedHandler;
import com.jala.backend.security.JwtAuthenticationEntryPoint;
import com.jala.backend.security.config.SecurityConfig;
import com.jala.backend.security.filter.JwtAuthenticationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Composite import that gives {@code @WebMvcTest} slices the production
 * security chain (filter, entry point, access-denied handler, rules)
 * and the global error contract.
 */
@TestConfiguration
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
public class WebSecurityTestConfig {
}
