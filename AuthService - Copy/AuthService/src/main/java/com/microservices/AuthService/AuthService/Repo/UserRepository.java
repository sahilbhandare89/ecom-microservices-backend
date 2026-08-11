package com.microservices.AuthService.AuthService.Repo;

import com.microservices.AuthService.AuthService.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(@Email(message = "Invalid email") @NotBlank(message = "Email is required") String email);
    Optional<User> findByEmail(String email);

//    boolean existsByEmail(String email);
}
