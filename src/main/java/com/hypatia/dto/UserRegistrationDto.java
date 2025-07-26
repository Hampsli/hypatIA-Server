package com.hypatia.dto;

import jakarta.validation.constraints.*;

/**
 * Data Transfer Object for user registration.
 *
 * This DTO captures all information required for user registration,
 * replacing specific 'age' with 'ageRange'.
 */
public class UserRegistrationDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    // --- OPTIMIZATION: Replaced 'age' (Integer) with 'ageRange' (String) ---
    @NotBlank(message = "Age range is required")
    @Size(max = 50, message = "Age range cannot exceed 50 characters") // Max length to match DB
    private String ageRange; // Now stores range string

    @NotBlank(message = "Current role is required")
    @Size(max = 100, message = "Current role cannot exceed 100 characters")
    private String currentRole;

    public UserRegistrationDto() {}

    public UserRegistrationDto(String name, String email, String password, String ageRange, String currentRole) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.ageRange = ageRange; // Use ageRange
        this.currentRole = currentRole;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name.trim() : null; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.toLowerCase().trim() : null; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    // --- OPTIMIZATION: Getter/Setter for ageRange ---
    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }
    // --- Removed getAge() and setAge() ---
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole != null ? currentRole.trim() : null; }

    @Override
    public String toString() {
        return "UserRegistrationDto{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", ageRange='" + ageRange + '\'' + // Show ageRange
                ", currentRole='" + currentRole + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationDto that = (UserRegistrationDto) o;
        return email != null ? email.equals(that.email) : that.email == null;
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}