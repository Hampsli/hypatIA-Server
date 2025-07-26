package com.hypatia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for initiating a password reset.
 * Contains the email address for which the password reset is requested.
 */
public class ForgotPasswordRequestDto {
    @NotBlank(message = "El email es requerido para restablecer la contraseña.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    // Default constructor
    public ForgotPasswordRequestDto() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.toLowerCase().trim() : null; }

    @Override
    public String toString() {
        return "ForgotPasswordRequestDto{" +
                "email='" + email + '\'' +
                '}';
    }
}