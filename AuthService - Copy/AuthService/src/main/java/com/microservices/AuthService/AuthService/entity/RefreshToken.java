package com.microservices.AuthService.AuthService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(
                        name = "idx_refresh_token_jti",
                        columnList = "jti",
                        unique = true
                ),
                @Index(
                        name = "idx_refresh_token_user_id",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "jti",
            nullable = false,
            unique = true,
            updatable = false
    )
    private String jti;

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "expires_at",
            nullable = false,
            updatable = false
    )
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token")
    private String replacedByToken;
}