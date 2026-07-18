package com.jala.backend.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jala.backend.auth.jwt.JwtService;
import com.jala.backend.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base for {@code @WebMvcTest} controller slices. Subclasses annotate
 * themselves with {@code @WebMvcTest(controllers = X.class)} and
 * {@code @Import(WebSecurityTestConfig.class)}; the JWT collaborators
 * are mocked here so the real security filter chain loads without a
 * database.
 */
public abstract class WebSliceTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;
}
