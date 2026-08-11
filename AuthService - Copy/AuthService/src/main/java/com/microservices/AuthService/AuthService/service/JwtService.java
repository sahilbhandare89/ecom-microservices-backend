package com.microservices.AuthService.AuthService.service;

import com.microservices.AuthService.AuthService.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer
    ) {

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret key cannot be null or empty"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    /**
     * Generate Access Token
     */
    public String generateToken(User user) {

        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusSeconds(accessTtlSeconds)
                        )
                )
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("type", "access")
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate a unique JWT ID for refresh token.
     */
    public String generateJti() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(User user, String jti) {

        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusSeconds(refreshTtlSeconds)
                        )
                )
                .claim("email", user.getEmail())
                .claim("type", "refresh")
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parse and validate JWT signature, issuer and expiration.
     */
    public Jws<Claims> parseToken(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Get claims from token.
     */
    private Claims getClaims(String token) {
        return parseToken(token).getPayload();
    }

    /**
     * Get User ID from JWT subject.
     */
    public UUID getUserId(String token) {

        return UUID.fromString(
                getClaims(token).getSubject()
        );
    }

    /**
     * Get email from JWT.
     */
    public String getEmail(String token) {

        return getClaims(token)
                .get("email", String.class);
    }

    /**
     * Get JWT ID.
     */
    public String getJti(String token) {

        return getClaims(token).getId();
    }

    /**
     * Check whether token is an access token.
     */
    public boolean isAccessToken(String token) {

        return "access".equals(
                getClaims(token).get("type", String.class)
        );
    }

    /**
     * Check whether token is a refresh token.
     */
    public boolean isRefreshToken(String token) {

        return "refresh".equals(
                getClaims(token).get("type", String.class)
        );
    }

    /**
     * Validate JWT.
     */
    public boolean validateToken(String token) {

        try {

            parseToken(token);

            return true;

        } catch (ExpiredJwtException e) {

            log.debug("JWT token expired");

        } catch (UnsupportedJwtException e) {

            log.warn("Unsupported JWT token");

        } catch (MalformedJwtException e) {

            log.warn("Malformed JWT token");

        } catch (IllegalArgumentException e) {

            log.warn("JWT token is null or empty");

        } catch (JwtException e) {

            log.warn(
                    "JWT validation failed: {}",
                    e.getMessage()
            );
        }

        return false;
    }

    /**
     * Validate specifically that the token is an access token.
     */
    public boolean validateAccessToken(String token) {

        try {

            return validateToken(token)
                    && isAccessToken(token);

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    /**
     * Validate specifically that the token is a refresh token.
     */
    public boolean validateRefreshToken(String token) {

        try {

            return validateToken(token)
                    && isRefreshToken(token);

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    /**
     * Access token lifetime.
     */
    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    /**
     * Refresh token lifetime.
     */
    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }
}