package com.hypatia.controller;

import com.hypatia.dto.UserProfileDto;
import com.hypatia.entity.User;
import com.hypatia.exception.NotFoundException;
import com.hypatia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller para operaciones de perfiles de usuario.
 * Se enfoca en manejar las peticiones HTTP y delegar la lógica de negocio al UserService.
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    @Autowired
    private UserService userService;

    /**
     * Obtiene el perfil del usuario autenticado.
     * La lógica para crear un perfil vacío si no existe está ahora en el servicio.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        User user = getAuthenticatedUser();
        Map<String, Object> profileResponse = userService.getUserProfileResponse(user);
        return ResponseEntity.ok(profileResponse);
    }

    /**
     * Crea o actualiza el perfil del usuario autenticado.
     * Delega toda la lógica de actualización y cálculo al servicio.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> updateUserProfile(@Valid @RequestBody UserProfileDto profileDto) {
        User user = getAuthenticatedUser();
        Map<String, Object> updatedProfileResponse = userService.updateUserProfile(user, profileDto);
        return ResponseEntity.ok(updatedProfileResponse);
    }

    /**
     * Obtiene el estado de completitud del perfil del usuario.
     */
    @GetMapping("/completion-status")
    public ResponseEntity<Map<String, Object>> getCompletionStatus() {
        User user = getAuthenticatedUser();
        Map<String, Object> completionStatus = userService.getProfileCompletionStatus(user);
        return ResponseEntity.ok(completionStatus);
    }

    /**
     * Obtiene los requerimientos y campos disponibles para el perfil.
     * Esta información es delegada al servicio para facilitar su mantenimiento.
     */
    @GetMapping("/requirements")
    public ResponseEntity<Map<String, Object>> getProfileRequirements() {
        return ResponseEntity.ok(userService.getProfileRequirements());
    }

    /**
     * Encapsula la obtención del usuario autenticado para evitar código repetido.
     *
     * @return El objeto User autenticado.
     * @throws NotFoundException si el usuario no se encuentra.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NotFoundException("No hay un usuario autenticado.");
        }
        String email = authentication.getName();
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado: " + email));
    }
}