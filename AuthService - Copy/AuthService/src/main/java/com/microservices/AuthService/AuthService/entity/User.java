package com.microservices.AuthService.AuthService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "user_email",
            unique = true,
            nullable = false,
            length = 300
    )
    private String email;

    @Column(
            name = "user_name",
            nullable = false,
            length = 500
    )
    private String name;

    @Column(nullable = false)
    private String password;

    private String image;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roles.stream()
                .map(role ->
                        new SimpleGrantedAuthority(
                                role.getName().name()
                        )
                )
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}