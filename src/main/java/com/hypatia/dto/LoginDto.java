package com.hypatia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user login credentials.
 * Captures the email and password submitted by a user attempting to log in.
 */
public class LoginDto {

    @NotBlank(message = "El email es requerido.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    @NotBlank(message = "La contraseña es requerida.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") // Minimum password length for validation
    private String password;

    // Default constructor for JSON deserialization
    public LoginDto() {}

    // Constructor with fields (optional, but good for testing)
    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null; // Normalize email
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginDto{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" + // Always hide sensitive info
                '}';
    }
}