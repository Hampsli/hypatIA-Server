package com.hypatia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for a user logout request.
 * Contains the JWT token to be invalidated (if server-side invalidation is active).
 */
public class LogoutRequestDto {
    @NotBlank(message = "El token es requerido para cerrar sesión.")
    private String token;

    // Default constructor
    public LogoutRequestDto() {}

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @Override
    public String toString() {
        return "LogoutRequestDto{" +
                "token='[HIDDEN]'" + // Hide token for security
                '}';
    }
}