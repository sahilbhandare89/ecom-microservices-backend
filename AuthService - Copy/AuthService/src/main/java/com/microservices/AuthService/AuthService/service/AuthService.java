package com.microservices.AuthService.AuthService.service;

import com.microservices.AuthService.AuthService.Repo.RefreshTokenRepository;
import com.microservices.AuthService.AuthService.Repo.RoleRepository;
import com.microservices.AuthService.AuthService.Repo.UserRepository;
import com.microservices.AuthService.AuthService.dto.AuthResponse;
import com.microservices.AuthService.AuthService.dto.RegisterRequest;
import com.microservices.AuthService.AuthService.dto.RegisterResponse;
import com.microservices.AuthService.AuthService.entity.Provider;
import com.microservices.AuthService.AuthService.entity.Role;
import com.microservices.AuthService.AuthService.entity.RoleType;
import com.microservices.AuthService.AuthService.entity.RefreshToken;
import com.microservices.AuthService.AuthService.entity.User;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role userRole = roleRepository
                .findByName(RoleType.ROLE_USER)
                .orElseThrow(() ->
                        new RuntimeException("Default USER role not found")
                );

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(Provider.LOCAL)
                .roles(Set.of(userRole))
                .enabled(true)
                .build();

        userRepository.save(user);

        return RegisterResponse.builder()
                .message("User registered successfully")
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public User findByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }

    @Transactional
    public AuthResponse generateTokens(User user) {

        // Access token
        String accessToken = jwtService.generateToken(user);

        // Generate unique JTI
        String jti = jwtService.generateJti();

        // Refresh JWT
        String refreshToken =
                jwtService.generateRefreshToken(user, jti);

        // Save refresh token in database
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(
                        Instant.now().plusSeconds(
                                jwtService.getRefreshTtlSeconds()
                        )
                )
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTtlSeconds())
                .build();
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {

        // Validate JWT
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String jti = jwtService.getJti(refreshToken);

        // Find active refresh token
        RefreshToken storedToken =
                refreshTokenRepository
                        .findByJtiAndRevokedAtIsNull(jti)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Refresh token revoked or not found"
                                )
                        );

        // Check DB expiry
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = storedToken.getUser();

        /*
         * ROTATION:
         * Revoke old refresh token.
         */
        storedToken.setRevokedAt(Instant.now());

        // Generate completely new token pair
        AuthResponse response = generateTokens(user);

        /*
         * Save which refresh token replaced this one.
         */
        String newJti = jwtService.getJti(response.getRefreshToken());

        storedToken.setReplacedByToken(newJti);

        refreshTokenRepository.save(storedToken);

        return response;
    }

    @Transactional
    public void logout(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {

            String jti = jwtService.getJti(refreshToken);

            refreshTokenRepository
                    .findByJti(jti)
                    .ifPresent(token -> {

                        if (token.getRevokedAt() == null) {
                            token.setRevokedAt(Instant.now());
                            refreshTokenRepository.save(token);
                        }
                    });

        } catch (JwtException | IllegalArgumentException e) {
            /*
             * Logout should be idempotent.
             *
             * If the token is already invalid/expired,
             * there is nothing useful to revoke.
             */
        }
    }
}