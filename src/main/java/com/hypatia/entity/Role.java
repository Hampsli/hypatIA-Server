package com.hypatia.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // --- Métodos de GrantedAuthority ---

    /**
     * Devuelve el nombre del rol. Spring Security usa este método para
     * verificar los permisos (ej. @PreAuthorize("hasAuthority('ROLE_ADMIN')")).
     */
    @Override
    public String getAuthority() {
        return name;
    }

    // --- Constructores, Getters y Setters ---

    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}