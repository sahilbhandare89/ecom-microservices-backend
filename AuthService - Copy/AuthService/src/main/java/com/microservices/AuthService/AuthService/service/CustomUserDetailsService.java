package com.microservices.AuthService.AuthService.service;

import com.microservices.AuthService.AuthService.Repo.UserRepository;
import com.microservices.AuthService.AuthService.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(
                        user.getRoles()
                                .stream()
                                .map(role -> role.getName().name())
                                .toArray(String[]::new)
                )
                .disabled(!user.isEnabled())
                .build();
    }
}