package com.hypatia.repository;

import com.hypatia.entity.Role;
import com.hypatia.entity.User;
import com.hypatia.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 *
 * Se enfoca en las operaciones de base de datos para la gestión
 * de la identidad, autenticación y autorización de usuarios.
 * Proporciona métodos para buscar usuarios por sus credenciales,
 * rol y estado en el flujo de la aplicación.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su dirección de email.
     * Este es el método principal para la autenticación y la verificación de existencia.
     *
     * @param email Email del usuario.
     * @return Un Optional que contiene al usuario si se encuentra.
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con un email específico.
     * Es más eficiente que findByEmail().isPresent().
     *
     * @param email Email a verificar.
     * @return true si el email ya está en uso, false en caso contrario.
     */
    boolean existsByEmail(String email);

    /**
     * Encuentra todos los usuarios que tienen un rol específico.
     * Útil para tareas administrativas como listar a todas las coaches o participantes.
     *
     * @param role El rol a buscar (ej. Role.ROLE_COACH).
     * @return Una lista de usuarios con ese rol.
     */
    List<User> findByRole(Role role);

    /**
     * Encuentra todos los usuarios que se encuentran en un estado específico del flujo.
     * Útil para encontrar, por ejemplo, a todas las participantes que están listas para coaching.
     *
     * @param status El estado a buscar (ej. UserStatus.COACHING_READY).
     * @return Una lista de usuarios en ese estado.
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Cuenta cuántos usuarios tienen un rol específico.
     *
     * @param role El rol a contar.
     * @return El número de usuarios con ese rol.
     */
    long countByRole(Role role);
}