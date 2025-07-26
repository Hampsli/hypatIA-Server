package com.hypatia.controller;

import com.hypatia.dto.AIRequestDto; // Updated generic request DTO
import com.hypatia.dto.AIResponseDto; // Generic AI response DTO
import com.hypatia.dto.AICleanupRequestDto; // Cleanup DTO remains
import com.hypatia.entity.AiInteraction;
import com.hypatia.entity.User;
import com.hypatia.exception.UserNotFoundException; // For custom user not found exception
import com.hypatia.service.AIService; // Now an HTTP client to FastAPI
import com.hypatia.service.PDFService;
import com.hypatia.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For @PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap; // Needed for Map.of if not using immutable maps directly
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for AI integration and services.
 * This version specifically optimizes for a generic FastAPI backend at /api/analizar,
 * accepting direct text input from the frontend.
 *
 * This controller acts as an intermediary, forwarding structured requests
 * to the FastAPI AI service and handling its generic responses.
 *
 * Base path: /api/ai
 *
 * Security:
 * - All endpoints require valid JWT authentication.
 * - AI interactions are user-scoped.
 * - Authorization for reports handled by @PreAuthorize.
 * - Error handling centralized by @ControllerAdvice (GlobalExceptionHandler).
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600) // Consider restricting origins in production
public class AIController {

    @Autowired
    private AIService aiService; // Now focuses on FastAPI communication

    @Autowired
    private PDFService pdfService; // Generates PDFs from generic AIResponseDto

    @Autowired
    private UserService userService; // To retrieve current user details

    /**
     * Helper method to get the currently authenticated User.
     * Assumes the user's email is the principal name in Spring Security context.
     * @return The authenticated User entity.
     * @throws UserNotFoundException if the user cannot be found.
     */
    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));
    }

    /**
     * Generic endpoint to request AI analysis.
     * The `text` input comes directly from the frontend, representing the full prompt for FastAPI.
     *
     * POST /api/ai/analyze
     *
     * @param requestDto DTO containing analysis type and the raw text input for the AI.
     * @return ResponseEntity with AI-generated generic analysis.
     * Errors are handled by GlobalExceptionHandler.
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeAI(@Valid @RequestBody AIRequestDto requestDto) {
        User user = getCurrentAuthenticatedUser();

        // Pass the analysisType and the direct text input to AIService
        AIResponseDto aiResponse = aiService.getGenericAIAnalysis(user.getId(), requestDto.getAnalysisType(), requestDto.getText());

        return ResponseEntity.ok(Map.of(
                "analysisType", requestDto.getAnalysisType(),
                "response", aiResponse.getResponseContent(), // Return the content from FastAPI
                "generatedAt", LocalDateTime.now(),
                // Provide links to download specific reports. These GET endpoints will
                // trigger a NEW call to FastAPI OR retrieve cached results from AIService.
                "actions", Map.of(
                        // Example: Construct download link; might need to encode parameters
                        "downloadReport", "/api/ai/reports/" + requestDto.getAnalysisType().toLowerCase() + "/" + user.getId() + "?text=" + requestDto.getText()
                        // If analysisType and text are enough for report regeneration, pass them as query params.
                )
        ));
    }


    /**
     * Downloads a generic AI-generated PDF report.
     * This endpoint is now more generic and handles different report types based on `reportType`.
     * The `text` parameter in the URL represents the original input used to generate the AI response.
     *
     * GET /api/ai/reports/{reportType}/{userId}
     *
     * @param reportType Type of report (e.g., "assessment_analysis", "career_recommendations", etc.).
     * @param userId The ID of the user for whom to generate the report.
     * @param text The original text input used for the AI analysis that generated the report content.
     * @return ResponseEntity with PDF file as a Resource.
     * Errors are handled by GlobalExceptionHandler.
     */
    @GetMapping("/reports/{reportType}/{userId}")
    @PreAuthorize("authentication.principal.username == @userService.findUserById(#userId).orElse(null)?.email or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadAIReport(
            @PathVariable String reportType,
            @PathVariable Long userId,
            @RequestParam @NotBlank(message = "Original text input is required for report generation.") String text) throws IOException { // The original text is now a required parameter
        User user = getCurrentAuthenticatedUser();

        // AIService needs to fetch or re-generate the AI response based on reportType and the original text
        // This will trigger a cache hit if the original analysis was cached.
        AIResponseDto aiResponse = aiService.getGenericAIAnalysis(userId, reportType, text);

        // PDFService now generates a PDF based on the generic AIResponseDto and the reportType
        // It might also need the original text or other params for formatting purposes
        Resource pdfResource = pdfService.generateGenericReport(userId, aiResponse, reportType, Map.of("originalText", text)); // Pass original text as a param for PDF generation if needed
        String filename = pdfService.generateReportFilename(reportType, user.getEmail());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfResource);
    }


    /**
     * Gets the authenticated user's AI interaction history.
     * This assumes AIInteraction logs are still maintained in the Spring App.
     *
     * GET /api/ai/history
     *
     * @return ResponseEntity with AI interaction history.
     * Errors are handled by GlobalExceptionHandler.
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAIHistory() {
        User user = getCurrentAuthenticatedUser();
        List<AiInteraction> history = aiService.getUserAIHistory(user.getId());

        return ResponseEntity.ok(Map.of(
                "history", history,
                "totalInteractions", history.size(),
                "lastInteraction", history.isEmpty() ? null : history.get(0).getCreatedAt() // Assuming history is sorted DESC
        ));
    }

    /**
     * Retrieves AI service statistics and usage analytics.
     * This endpoint might require an ADMIN role for access.
     *
     * GET /api/ai/stats
     *
     * @return ResponseEntity with AI service statistics.
     * Errors are handled by GlobalExceptionHandler.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can view stats
    public ResponseEntity<Map<String, Object>> getAIServiceStats() {
        Map<String, Object> stats = aiService.getAIServiceStatistics();

        return ResponseEntity.ok(Map.of(
                "stats", stats,
                "retrievedAt", LocalDateTime.now()
        ));
    }

    /**
     * Triggers cleanup of old AI interactions (administrative endpoint).
     * This endpoint typically requires an ADMIN role for access.
     *
     * DELETE /api/ai/cleanup
     *
     * @param requestDto DTO containing cleanup parameters (e.g., "olderThanDays").
     * @return ResponseEntity with cleanup results.
     * Errors are handled by GlobalExceptionHandler.
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can trigger cleanup
    public ResponseEntity<Map<String, Object>> cleanupOldInteractions(@RequestBody AICleanupRequestDto requestDto) {
        int cleanedUp = aiService.cleanupOldInteractions(requestDto.getOlderThanDays());

        return ResponseEntity.ok(Map.of(
                "message", "AI interactions cleaned up successfully",
                "removedCount", cleanedUp,
                "olderThanDays", requestDto.getOlderThanDays()
        ));
    }
}