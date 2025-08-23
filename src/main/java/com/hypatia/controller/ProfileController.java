package com.hypatia.controller;

import com.hypatia.dto.UserProfileDto;
import com.hypatia.entity.User;
import com.hypatia.exception.NotFoundException;
import com.hypatia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@Valid @RequestBody UserProfileDto profileDto) {
        User user = getAuthenticatedUser();
        Map<String, Object> updatedProfileResponse = userService.updateUserProfile(user, profileDto);
        return ResponseEntity.ok(updatedProfileResponse);
    }

    @PostMapping("/cv")
    public ResponseEntity<String>saveCv(@RequestParam("cv")MultipartFile cv) throws IOException {
        User user = getAuthenticatedUser();
        String response= userService.updloadCv(cv,user);
        if (response.contains("problema")) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (response.contains("logueate")) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else if (response.equals("Solo se permiten archivos PDF") || response.equals("El archivo debe tener extensión .pdf")) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } 
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/cv/{id}")
    public ResponseEntity<byte[]> getCv(@PathVariable Long id){
        return userService.getCv(id);
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