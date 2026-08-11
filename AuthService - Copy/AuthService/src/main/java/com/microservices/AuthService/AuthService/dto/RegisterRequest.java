package com.microservices.AuthService.AuthService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 3,
            max = 100,
            message = "Password must contain between 8 and 100 characters"
    )
    private String password;
}