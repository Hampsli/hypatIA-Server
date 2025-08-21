package com.hypatia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "public", indexes = {
        @Index(name = "idx_user_email", columnList = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "El email es requerido")
    @Email(message = "Por favor, proporciona un email válido")
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "La contraseña es requerida")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role_User role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    public User() {
        this.role = Role_User.ROLE_PARTICIPANT;
        this.status = UserStatus.PENDING_ONBOARDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role_User getRole() { return role; }
    public void setRole(Role_User role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

    // --- OPTIMIZATION: Getter for name from UserProfile ---
    public String getName() {
        return userProfile != null ? userProfile.getName() : null;
    }

    // --- OPTIMIZATION: Getter for ageRange from UserProfile ---
    public String getAgeRange() { // Renamed from getAge()
        return userProfile != null ? userProfile.getAgeRange() : null;
    }

    @Override
    public String toString() {
        String userName = (userProfile != null && userProfile.getName() != null) ? userProfile.getName() : "N/A";
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + userName + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}