package com.hypatia.entity;

/**
 * Entidad que representa un rol o una autoridad en el sistema.
 * Implementa GrantedAuthority para una integración directa con Spring Security.
 */

public enum Role_User {
    ROLE_PARTICIPANT,
    ROLE_ADMIN,
    ROLE_COACH
}
