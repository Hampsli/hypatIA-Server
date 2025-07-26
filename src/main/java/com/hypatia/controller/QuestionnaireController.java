package com.hypatia.controller;

import com.hypatia.dto.QuestionnaireDto;
import com.hypatia.dto.QuestionnaireResponseDto;
import com.hypatia.entity.User;
import com.hypatia.service.QuestionnaireService;
import com.hypatia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller para operaciones de cuestionarios.
 *
 * Gestiona el ciclo de vida completo de los cuestionarios basados en fases, incluyendo:
 * - Obtención de la estructura completa del cuestionario (secciones, preguntas y opciones).
 * - Envío y validación de respuestas.
 * - Seguimiento del progreso y estado de finalización por cuestionario.
 *
 * Base path: /api/questionnaires
 *
 * Seguridad:
 * - Todos los endpoints requieren autenticación JWT válida.
 * - Los usuarios solo pueden acceder a los datos de sus propios cuestionarios.
 */
@RestController
@RequestMapping("/api/questionnaires")
@CrossOrigin(origins = "*", maxAge = 3600)
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private UserService userService;

    /**
     * Obtiene la estructura completa de un cuestionario por su fase.
     *
     * Devuelve un objeto anidado con toda la información del cuestionario,
     * incluyendo sus secciones, preguntas y opciones de respuesta, listo
     * para ser renderizado por una aplicación cliente.
     *
     * GET /api/questionnaires/{phase}
     *
     * @param phase La fase del cuestionario a obtener (ej. "onboarding").
     * @return ResponseEntity con el DTO del cuestionario completo.
     */
    @GetMapping("/{phase}")
    public ResponseEntity<?> getQuestionnaireByPhase(@PathVariable String phase) {
        try {
            Optional<QuestionnaireDto> questionnaireDto = questionnaireService.getQuestionnaireByPhase(phase);
            if (questionnaireDto.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Questionnaire for phase '" + phase + "' not found."));
            }
            return ResponseEntity.ok(questionnaireDto.get());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve questionnaire for phase: " + phase));
        }
    }

    /**
     * Envía las respuestas de un cuestionario para el usuario autenticado.
     *
     * Acepta un conjunto de respuestas, las valida y las guarda, actualizando
     * el progreso del usuario para ese cuestionario específico.
     *
     * POST /api/questionnaires/responses
     *
     * @param responses Una lista de DTOs de respuesta.
     * @return ResponseEntity con el resultado del envío y el progreso actualizado.
     */
    @PostMapping("/responses")
    public ResponseEntity<?> submitQuestionnaireResponses(@Valid @RequestBody List<QuestionnaireResponseDto> responses) {
        try {
            User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));

            if (responses == null || responses.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No responses provided"));
            }

            // Asumimos que todas las respuestas son para el mismo cuestionario.
            // Extraemos la fase del cuestionario a partir de la primera pregunta.
            String questionnairePhase = questionnaireService.getQuestionnairePhaseFromQuestionId(responses.get(0).getQuestionId());

            questionnaireService.saveUserResponses(user, responses);

            // Obtener el progreso actualizado para ese cuestionario
            double completionPercentage = questionnaireService.getCompletionPercentage(user, questionnairePhase);
            boolean isCompleted = completionPercentage >= 100.0;

            return ResponseEntity.ok(Map.of(
                    "message", "Responses submitted successfully for questionnaire: " + questionnairePhase,
                    "submittedCount", responses.size(),
                    "completionPercentage", completionPercentage,
                    "isCompleted", isCompleted
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to submit responses. " + e.getMessage()));
        }
    }

    /**
     * Obtiene las respuestas de un usuario para un cuestionario específico.
     *
     * GET /api/questionnaires/responses/{phase}
     *
     * @param phase La fase del cuestionario cuyas respuestas se desean obtener.
     * @return ResponseEntity con la lista de respuestas del usuario.
     */
    @GetMapping("/responses/{phase}")
    public ResponseEntity<?> getUserResponsesForQuestionnaire(@PathVariable String phase) {
        try {
            User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));

            // Aquí el servicio devolvería un DTO que represente las respuestas guardadas.
            var userResponses = questionnaireService.getUserResponses(user, phase);

            return ResponseEntity.ok(Map.of(
                    "phase", phase,
                    "responses", userResponses
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve responses for phase: " + phase));
        }
    }

    /**
     * Obtiene el progreso de un usuario en un cuestionario específico.
     *
     * GET /api/questionnaires/progress/{phase}
     *
     * @param phase La fase del cuestionario cuyo progreso se desea consultar.
     * @return ResponseEntity con los detalles del progreso.
     */
    @GetMapping("/progress/{phase}")
    public ResponseEntity<?> getQuestionnaireProgress(@PathVariable String phase) {
        try {
            User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> progress = questionnaireService.getProgress(user, phase);

            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to retrieve progress for phase: " + phase));
        }
    }

    /**
     * Reinicia las respuestas de un usuario para un cuestionario, permitiendo volver a tomarlo.
     *
     * DELETE /api/questionnaires/responses/{phase}
     *
     * @param phase La fase del cuestionario que se va a reiniciar.
     * @return ResponseEntity con la confirmación del reinicio.
     */
    @DeleteMapping("/responses/{phase}")
    public ResponseEntity<?> resetQuestionnaire(@PathVariable String phase) {
        try {
            User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));

            long removedCount = questionnaireService.resetUserResponses(user, phase);

            return ResponseEntity.ok(Map.of(
                    "message", "Responses for questionnaire '" + phase + "' have been reset.",
                    "removedResponsesCount", removedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to reset questionnaire: " + phase));
        }
    }

    /**
     * Obtiene el usuario autenticado actualmente a través del contexto de seguridad.
     *
     * @return Un Optional que contiene al User si está autenticado.
     */
    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String email = authentication.getName();
        return userService.findUserByEmail(email);
    }
}