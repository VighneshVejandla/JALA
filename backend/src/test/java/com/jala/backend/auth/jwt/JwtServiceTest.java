package com.jala.backend.auth.jwt;

import com.jala.backend.config.JwtProperties;
import com.jala.backend.role.entity.Role;
import com.jala.backend.security.model.CustomUserDetails;
import com.jala.backend.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef";

    private static final long ONE_HOUR_MILLIS = 3_600_000L;

    private JwtService jwtService(long expirationMillis) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpiration(expirationMillis);
        return new JwtService(properties);
    }

    private User user(String employeeCode, boolean active, int tokenVersion) {
        return User.builder()
                .id(UUID.randomUUID())
                .employeeCode(employeeCode)
                .fullName("Test User")
                .passwordHash("hash")
                .isActive(active)
                .tokenVersion(tokenVersion)
                .role(Role.builder()
                        .id(UUID.randomUUID())
                        .name("WORKER")
                        .build())
                .build();
    }

    @Test
    void generateTokenEmbedsSubjectAndTokenVersionClaim() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        CustomUserDetails details =
                new CustomUserDetails(user("EMP001", true, 7));

        String token = service.generateToken(details);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("EMP001");
        assertThat(claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class))
                .isEqualTo(7);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void extractUsernameReturnsSubject() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        String token = service.generateToken(
                new CustomUserDetails(user("EMP042", true, 0)));

        assertThat(service.extractUsername(token)).isEqualTo("EMP042");
    }

    @Test
    void isTokenValidReturnsTrueForMatchingActiveUser() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        CustomUserDetails details =
                new CustomUserDetails(user("EMP001", true, 3));

        String token = service.generateToken(details);

        assertThat(service.isTokenValid(token, details)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseWhenUsernameDoesNotMatch() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        String token = service.generateToken(
                new CustomUserDetails(user("EMP001", true, 0)));

        CustomUserDetails otherUser =
                new CustomUserDetails(user("EMP002", true, 0));

        assertThat(service.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseWhenUserIsDisabled() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        User user = user("EMP001", true, 0);
        CustomUserDetails details = new CustomUserDetails(user);

        String token = service.generateToken(details);

        user.setIsActive(false);

        assertThat(service.isTokenValid(token, details)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseWhenTokenVersionWasBumpedAfterIssue() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        User user = user("EMP001", true, 1);
        CustomUserDetails details = new CustomUserDetails(user);

        String token = service.generateToken(details);

        user.setTokenVersion(2);

        assertThat(service.isTokenValid(token, details)).isFalse();
    }

    @Test
    void expiredTokenThrowsJwtExceptionOnParsing() {
        JwtService expiredIssuer = jwtService(-ONE_HOUR_MILLIS);
        CustomUserDetails details =
                new CustomUserDetails(user("EMP001", true, 0));

        String expiredToken = expiredIssuer.generateToken(details);

        JwtService verifier = jwtService(ONE_HOUR_MILLIS);

        assertThatThrownBy(() -> verifier.extractUsername(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);

        assertThatThrownBy(() -> verifier.isTokenValid(expiredToken, details))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedTokenThrowsJwtException() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);
        CustomUserDetails details =
                new CustomUserDetails(user("EMP001", true, 0));

        String token = service.generateToken(details);

        String[] parts = token.split("\\.");
        char first = parts[2].charAt(0);
        parts[2] = (first == 'a' ? 'b' : 'a') + parts[2].substring(1);
        String tampered = String.join(".", parts);

        assertThatThrownBy(() -> service.extractUsername(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void garbageTokenThrowsJwtException() {
        JwtService service = jwtService(ONE_HOUR_MILLIS);

        assertThatThrownBy(() -> service.extractUsername("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }
}
