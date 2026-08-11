package com.microservices.AuthService.AuthService.controller;


import com.microservices.AuthService.AuthService.dto.*;
import com.microservices.AuthService.AuthService.entity.User;
import com.microservices.AuthService.AuthService.service.AuthService;
import com.microservices.AuthService.AuthService.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;


   @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = authService.findByEmail(
                    request.getEmail()
            );

            AuthResponse response =
                    authService.generateTokens(user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error(
                    "Authentication failed for user: {}",
                    request.getEmail()
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        try {

            AuthResponse response =
                    authService.refreshAccessToken(
                            request.getRefreshToken()
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "Logged out successfully"
                )
        );
    }
}
