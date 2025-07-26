package com.hypatia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for requesting JWT token validation.
 * Contains the JWT token to be validated.
 */
public class TokenValidationRequestDto {
    @NotBlank(message = "El token es requerido para la validación.")
    private String token;

    // Default constructor
    public TokenValidationRequestDto() {}

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @Override
    public String toString() {
        return "TokenValidationRequestDto{" +
                "token='[HIDDEN]'" + // Hide token
                '}';
    }
}