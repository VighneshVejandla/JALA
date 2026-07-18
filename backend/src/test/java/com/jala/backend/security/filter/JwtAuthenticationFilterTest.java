package com.jala.backend.security.filter;

import com.jala.backend.auth.jwt.JwtService;
import com.jala.backend.role.entity.Role;
import com.jala.backend.security.model.CustomUserDetails;
import com.jala.backend.security.service.CustomUserDetailsService;
import com.jala.backend.user.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CustomUserDetails userDetails(String employeeCode, String roleName) {
        return new CustomUserDetails(User.builder()
                .id(UUID.randomUUID())
                .employeeCode(employeeCode)
                .fullName("Test User")
                .passwordHash("hash")
                .isActive(true)
                .tokenVersion(0)
                .role(Role.builder()
                        .id(UUID.randomUUID())
                        .name(roleName)
                        .build())
                .build());
    }

    @Test
    void continuesUnauthenticatedWhenNoAuthorizationHeader() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void continuesUnauthenticatedWhenHeaderIsNotBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void populatesSecurityContextForValidToken() throws Exception {
        CustomUserDetails details = userDetails("EMP001", "WORKER");
        request.addHeader("Authorization", "Bearer good-token");
        when(jwtService.extractUsername("good-token")).thenReturn("EMP001");
        when(userDetailsService.loadUserByUsername("EMP001"))
                .thenReturn(details);
        when(jwtService.isTokenValid("good-token", details)).thenReturn(true);

        filter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(details);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_WORKER");
        assertThat(authentication.getDetails()).isNotNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void continuesUnauthenticatedWhenJwtParsingFails() throws Exception {
        request.addHeader("Authorization", "Bearer broken-token");
        when(jwtService.extractUsername("broken-token"))
                .thenThrow(new JwtException("bad signature"));

        assertThatCode(() -> filter.doFilter(request, response, chain))
                .doesNotThrowAnyException();

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void continuesUnauthenticatedWhenUserNoLongerExists() throws Exception {
        request.addHeader("Authorization", "Bearer orphan-token");
        when(jwtService.extractUsername("orphan-token")).thenReturn("GONE");
        when(userDetailsService.loadUserByUsername("GONE"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThatCode(() -> filter.doFilter(request, response, chain))
                .doesNotThrowAnyException();

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void doesNotAuthenticateWhenTokenIsInvalid() throws Exception {
        CustomUserDetails details = userDetails("EMP001", "WORKER");
        request.addHeader("Authorization", "Bearer stale-token");
        when(jwtService.extractUsername("stale-token")).thenReturn("EMP001");
        when(userDetailsService.loadUserByUsername("EMP001"))
                .thenReturn(details);
        when(jwtService.isTokenValid("stale-token", details))
                .thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void doesNotAuthenticateWhenExtractedUsernameIsNull() throws Exception {
        request.addHeader("Authorization", "Bearer subjectless-token");
        when(jwtService.extractUsername("subjectless-token")).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void keepsExistingAuthenticationUntouched() throws Exception {
        Authentication existing = new UsernamePasswordAuthenticationToken(
                "already-there", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        request.addHeader("Authorization", "Bearer another-token");
        when(jwtService.extractUsername("another-token")).thenReturn("EMP001");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isSameAs(existing);
        verify(jwtService).extractUsername(anyString());
        verifyNoInteractions(userDetailsService);
        assertThat(chain.getRequest()).isSameAs(request);
    }
}
