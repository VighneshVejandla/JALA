package com.jala.backend.auth.jwt;

import com.jala.backend.config.JwtProperties;
import com.jala.backend.security.model.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
// The jjwt builder API (issuedAt/expiration) takes java.util.Date, so the
// java.time-only rule cannot be satisfied here.
@SuppressWarnings("java:S2143")
public class JwtService {

    /** Claim carrying the user's token version at issue time. */
    static final String CLAIM_TOKEN_VERSION = "ver";

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(CustomUserDetails userDetails) {

        Instant now = Instant.now();

        Instant expiry = now.plusMillis(
                jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TOKEN_VERSION,
                        userDetails.getUser().getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * @throws io.jsonwebtoken.JwtException if the token is malformed,
     *         expired or has an invalid signature — callers must handle it.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * A token is valid only when the subject matches, the account is still
     * enabled, the embedded token version equals the user's current version
     * (revocation check) and the token has not expired.
     */
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {

        Claims claims = extractAllClaims(token);

        Integer issuedVersion =
                claims.get(CLAIM_TOKEN_VERSION, Integer.class);

        return userDetails.getUsername().equals(claims.getSubject())
                && userDetails.isEnabled()
                && Objects.equals(issuedVersion,
                        userDetails.getUser().getTokenVersion())
                && claims.getExpiration().toInstant().isAfter(Instant.now());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
