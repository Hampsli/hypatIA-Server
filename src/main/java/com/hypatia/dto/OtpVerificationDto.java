package com.hypatia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for One-Time Password (OTP) verification.
 * Captures the email and the OTP code submitted by the user.
 */
public class OtpVerificationDto {

    @NotBlank(message = "El email es requerido para la verificación OTP.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    @NotBlank(message = "El código OTP es requerido.")
    @Pattern(regexp = "\\d{6}", message = "El código OTP debe ser de exactamente 6 dígitos.") // Enforce 6 digits
    private String otp;

    // Default constructor
    public OtpVerificationDto() {}

    // Constructor with fields
    public OtpVerificationDto(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null; // Normalize email
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public String toString() {
        return "OtpVerificationDto{" +
                "email='" + email + '\'' +
                ", otp='[HIDDEN]'" + // Hide OTP for security
                '}';
    }
}