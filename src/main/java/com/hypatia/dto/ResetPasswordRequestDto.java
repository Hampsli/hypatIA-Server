package com.hypatia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for completing a password reset.
 * Contains the email, OTP, and the new password for verification and update.
 */
public class ResetPasswordRequestDto {
    @NotBlank(message = "El email es requerido.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    @NotBlank(message = "El código OTP es requerido.")
    @Pattern(regexp = "\\d{6}", message = "El código OTP debe ser de exactamente 6 dígitos.") // Assuming 6 digits
    private String otp;

    @NotBlank(message = "La nueva contraseña es requerida.")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres.") // Minimum password length
    private String newPassword;

    // Default constructor
    public ResetPasswordRequestDto() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.toLowerCase().trim() : null; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    @Override
    public String toString() {
        return "ResetPasswordRequestDto{" +
                "email='" + email + '\'' +
                ", otp='[HIDDEN]'" + // Hide OTP
                ", newPassword='[PROTECTED]'" + // Hide new password
                '}';
    }
}