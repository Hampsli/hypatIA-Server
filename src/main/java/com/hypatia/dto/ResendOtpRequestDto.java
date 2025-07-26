package com.hypatia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for requesting to resend an OTP.
 * Includes the email and an optional purpose for the OTP (e.g., login, password reset).
 */
public class ResendOtpRequestDto {
    @NotBlank(message = "El email es requerido.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    // Optional: Add validation if 'purpose' has specific allowed values.
    // Ensure this matches values used in EmailService (e.g., "LOGIN_VERIFICATION", "PASSWORD_RESET").
    @Pattern(regexp = "EMAIL_VERIFICATION|LOGIN_VERIFICATION|PASSWORD_RESET", message = "El propósito debe ser EMAIL_VERIFICATION, LOGIN_VERIFICATION o PASSWORD_RESET.")
    private String purpose = "LOGIN_VERIFICATION"; // Default value

    // Default constructor
    public ResendOtpRequestDto() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.toLowerCase().trim() : null; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    @Override
    public String toString() {
        return "ResendOtpRequestDto{" +
                "email='" + email + '\'' +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}