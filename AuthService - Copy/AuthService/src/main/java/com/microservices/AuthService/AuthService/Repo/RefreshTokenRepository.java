package com.microservices.AuthService.AuthService.Repo;

import com.microservices.AuthService.AuthService.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJti(String jti);

    Optional<RefreshToken> findByJtiAndRevokedAtIsNull(String jti);
}