package com.hypatia.dto;

/**
 * DTO para mostrar la información pública de un usuario de forma segura.
 * NUNCA incluye la contraseña.
 */
public class UserDto {

    private Long id;
    private String email;
    private String role;
    private String status;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}