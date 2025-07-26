package com.hypatia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypatia.dto.AIResponseDto;
import com.hypatia.entity.User;
import com.hypatia.entity.UserProfile;
import com.hypatia.exception.UserNotFoundException;
import com.hypatia.repository.UserRepository;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap; // To preserve insertion order for grouped skills
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors; // For stream operations

/**
 * Service class for PDF generation and report creation.
 * Optimized to generate ONLY a single "Profile Summary" report,
 * incorporating user data, questionnaire answers, and parsed AI analysis.
 *
 * @author hypatIA Development Team
 */
@Service
@Transactional
public class PDFService {

    private static final Logger log = LoggerFactory.getLogger(PDFService.class);

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // To get UserProfile

    @Autowired
    private QuestionnaireService questionnaireService; // To get assessment data (questions/answers)

    private final ObjectMapper objectMapper = new ObjectMapper(); // For parsing AIResponseDto content

    @Value("${hypatia.app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${hypatia.organization.name:hypatIA}")
    private String organizationName;

    @Value("${hypatia.report.footer:© 2024 hypatIA. Empowering women in STEM.}")
    private String reportFooter;

    /**
     * Generates a comprehensive "Profile Summary" PDF report for a user,
     * including questionnaire answers and parsed AI analysis results.
     * This method now consolidates all report generation logic into a single type.
     *
     * @param userId The ID of the user for whom the report is generated.
     * @param aiResponse The generic AI response DTO (contains raw JSON from FastAPI).
     * @param reportType This parameter is now fixed to "PROFILE_SUMMARY".
     * @param reportSpecificParams Not used directly for this specific report, but kept for signature consistency.
     * @return PDF report as ByteArrayResource.
     * @throws UserNotFoundException if user doesn't exist.
     * @throws IOException if PDF generation fails.
     * @throws IllegalArgumentException if reportType is not "PROFILE_SUMMARY" or AI data is malformed.
     */
    public Resource generateGenericReport(Long userId, AIResponseDto aiResponse, String reportType, Map<String, String> reportSpecificParams) throws IOException {
        // Enforce that only "PROFILE_SUMMARY" reports are generated
        if (!"PROFILE_SUMMARY".equalsIgnoreCase(reportType)) {
            log.error("Unsupported report type requested: {}. Only 'PROFILE_SUMMARY' is supported.", reportType);
            throw new IllegalArgumentException("Unsupported report type. Only 'PROFILE_SUMMARY' reports can be generated.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for PDF report generation: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });

        Map<String, Object> templateData = new HashMap<>();

        // --- 1. Populate Common Report Data ---
        populateCommonReportData(templateData, user, "Resumen de Perfil Profesional"); // Display name for report type

        // --- 2. Populate User Profile and Assessment Data ---
        populateUserProfileAndAssessmentData(templateData, user);

        // --- 3. Populate Questionnaire Questions and Answers ---
        try {
            // Assuming questionnaireService provides a method to get questions and answers
            List<Map<String, Object>> userQuestionsAndAnswers = questionnaireService.getUserQuestionsAndAnswers(user);
            templateData.put("questionsAndAnswers", userQuestionsAndAnswers);
            log.debug("Added {} questions and answers for user {} to report.", userQuestionsAndAnswers.size(), userId);
        } catch (Exception e) {
            log.warn("Could not load questionnaire data for user {}: {}", userId, e.getMessage());
            templateData.put("questionsAndAnswers", new ArrayList<>()); // Provide empty list
        }

        // --- 4. Parse and Integrate AI Analysis Content ---
        if (aiResponse != null && aiResponse.getResponseContent() != null) {
            Map<String, Object> aiContent = aiResponse.getResponseContent();
            templateData.put("aiRawContent", aiContent); // For debugging or full raw display

            // Extract and format original feedback text
            if (aiContent.containsKey("retroalimentacion_original")) {
                templateData.put("retroalimentacionOriginal", aiContent.get("retroalimentacion_original").toString());
            } else {
                templateData.put("retroalimentacionOriginal", "No se proporcionó retroalimentación original del análisis de IA.");
            }

            // Extract and format analyzed skills
            if (aiContent.containsKey("habilidades_analizadas") && aiContent.get("habilidades_analizadas") instanceof List) {
                List<Map<String, String>> rawSkills = (List<Map<String, String>>) aiContent.get("habilidades_analizadas");
                // Group skills by 'competencia_ligada'
                Map<String, List<Map<String, String>>> groupedSkills = rawSkills.stream()
                        .sorted(Comparator.comparing(s -> s.getOrDefault("competencia_ligada", ""))) // Sort by competency
                        .collect(Collectors.groupingBy(
                                s -> s.getOrDefault("competencia_ligada", "Sin Competencia Ligada"),
                                LinkedHashMap::new, // Preserve insertion order of groups
                                Collectors.toList()
                        ));
                templateData.put("habilidadesAnalizadasAgrupadas", groupedSkills);
            } else {
                templateData.put("habilidadesAnalizadasAgrupadas", new LinkedHashMap<>());
                log.warn("AI response missing or malformed 'habilidades_analizadas' for user {}", userId);
            }

            // Extract and format suggested methodologies
            if (aiContent.containsKey("metodologias_sugeridas") && aiContent.get("metodologias_sugeridas") instanceof List) {
                List<Map<String, String>> suggestedMethods = (List<Map<String, String>>) aiContent.get("metodologias_sugeridas");
                templateData.put("metodologiasSugeridas", suggestedMethods);
            } else {
                templateData.put("metodologiasSugeridas", new ArrayList<>());
                log.warn("AI response missing or malformed 'metodologias_sugeridas' for user {}", userId);
            }

            log.debug("Parsed AI analysis for user {}. Keys: {}, Skills: {}, Methods: {}",
                    userId, aiContent.keySet(),
                    templateData.get("habilidadesAnalizadasAgrupadas") != null ? ((Map) templateData.get("habilidadesAnalizadasAgrupadas")).size() : 0,
                    templateData.get("metodologiasSugeridas") != null ? ((List) templateData.get("metodologiasSugeridas")).size() : 0);

        } else {
            log.warn("AIResponseDto is null or empty for report generation for user {}", userId);
            templateData.put("aiRawContent", Map.of("error", "No se proporcionó contenido de análisis de IA."));
            templateData.put("retroalimentacionOriginal", "N/A");
            templateData.put("habilidadesAnalizadasAgrupadas", new LinkedHashMap<>());
            templateData.put("metodologiasSugeridas", new ArrayList<>());
        }

        // --- 5. Generate PDF ---
        log.info("Generating Profile Summary PDF report for user {}", userId);
        return generatePDFFromTemplate("profile-summary-report", templateData); // Use a new specific template name
    }


    /**
     * Populates common data for all PDF reports.
     * @param templateData The map to populate.
     * @param user The user entity.
     * @param reportTypeDisplayName The display name of the report type.
     */
    private void populateCommonReportData(Map<String, Object> templateData, User user, String reportTypeDisplayName) {
        templateData.put("user", user);
        templateData.put("generatedAt", LocalDateTime.now());
        templateData.put("reportTypeDisplay", reportTypeDisplayName);
        templateData.put("organizationName", organizationName);
        templateData.put("baseUrl", baseUrl);
        templateData.put("footer", reportFooter);
    }

    /**
     * Populates user profile and assessment data for PDF reports.
     * @param templateData The map to populate.
     * @param user The user entity.
     */
    private void populateUserProfileAndAssessmentData(Map<String, Object> templateData, User user) {
        try {
            UserProfile profile = userService.getUserProfile(user); // Assuming this returns UserProfile or throws if not found
            templateData.put("profile", profile);
        } catch (Exception e) {
            log.warn("Could not load user profile for PDF report for user {}: {}", user.getId(), e.getMessage());
            templateData.put("profile", null); // Set to null if profile cannot be loaded
        }

        try {
            // Assuming these methods exist and return valid data or null/empty if not applicable
            // For profile summary, we might want ALL assessment questions/answers rather than just summary/scores.
            // Ensure questionnaireService.getUserQuestionsAndAnswers(userId) provides what's needed.
            Optional<Map<String, Object>> assessmentSummary = questionnaireService.getAssessmentSummary(user);
            templateData.put("assessmentSummary", assessmentSummary);

            Map<String, Double> categoryScores = questionnaireService.calculateCategoryScores(user);
            templateData.put("categoryScores", categoryScores);

            Double completionPercentage = questionnaireService.getAssessmentCompletionPercentage(user);
            templateData.put("completionPercentage", completionPercentage);
        } catch (Exception e) {
            log.warn("Could not load assessment data for PDF report for user {}: {}", user.getId(), e.getMessage());
            templateData.put("assessmentSummary", null);
            templateData.put("categoryScores", null);
            templateData.put("completionPercentage", null);
        }
    }


    /**
     * Core method to generate PDF from Thymeleaf template.
     *
     * @param templateName Thymeleaf template name (e.g., "profile-summary-report").
     * @param templateData data context for template processing.
     * @return PDF as ByteArrayResource.
     * @throws IOException if PDF generation fails.
     */
    private Resource generatePDFFromTemplate(String templateName, Map<String, Object> templateData) throws IOException {
        try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) { // Use try-with-resources
            // Process Thymeleaf template
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateData);

            // Template paths are relative to src/main/resources/templates/pdf/
            String htmlContent = templateEngine.process("pdf/" + templateName, thymeleafContext);

            // Configure PDF converter properties
            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setCharset("UTF-8");

            // Generate PDF
            HtmlConverter.convertToPdf(htmlContent, pdfOutputStream, converterProperties);

            log.info("PDF generado exitosamente desde la plantilla: {}", templateName);
            return new ByteArrayResource(pdfOutputStream.toByteArray());

        } catch (Exception e) {
            log.error("Fallo al generar el reporte PDF desde la plantilla {}: {}", templateName, e.getMessage(), e);
            throw new IOException("Fallo al generar el reporte PDF desde la plantilla " + templateName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Generates filename for PDF report based on type and user.
     *
     * @param reportType type of report.
     * @param userEmail user's email for filename (more robust than name).
     * @return formatted filename.
     */
    public String generateReportFilename(String reportType, String userEmail) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedReportType = reportType.replaceAll("[^a-zA-Z0-9_.-]", "_").replace(" ", "_");
        String sanitizedEmail = userEmail.replaceAll("[^a-zA-Z0-9.-]", "_at_"); // Replace @ for safer filenames

        return String.format("%s_%s_%s.pdf",
                sanitizedReportType,
                sanitizedEmail,
                timestamp);
    }

    /**
     * Gets PDF service configuration and status.
     *
     * @return configuration information.
     */
    public Map<String, Object> getPDFServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("templateEngine", templateEngine != null ? "Available" : "Not configured");
        status.put("organizationName", organizationName);
        status.put("baseUrl", baseUrl);
        status.put("pdfQuality", "Managed by iText defaults/configuration");
        status.put("supportedReportTypes", List.of("PROFILE_SUMMARY")); // Only PROFILE_SUMMARY is supported now

        return status;
    }

    /**
     * Validates PDF generation requirements.
     * This method is now simplified for only "PROFILE_SUMMARY" reports.
     *
     * @param userId user ID to validate.
     * @param reportType type of report to validate.
     * @throws IllegalArgumentException if validation fails.
     * @throws UserNotFoundException if user does not exist.
     */
    public void validatePDFGenerationRequirements(Long userId, String reportType) {
        if (!"PROFILE_SUMMARY".equalsIgnoreCase(reportType)) {
            throw new IllegalArgumentException("Unsupported report type: " + reportType + ". Only 'PROFILE_SUMMARY' is supported.");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // For Profile Summary, ensure user profile and some assessment data exist
        try {
            userService.getUserProfile(user); // Just try to fetch it
        } catch (Exception e) {
            throw new IllegalArgumentException("User profile data is required for Profile Summary report: " + e.getMessage(), e);
        }

        try {
            // Check if there are any questions/answers to include
            List<Map<String, Object>> qanda = questionnaireService.getUserQuestionsAndAnswers(user);
            if (qanda == null || qanda.isEmpty()) {
                throw new IllegalArgumentException("Questionnaire answers are required for Profile Summary report.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Questionnaire data required for Profile Summary report: " + e.getMessage(), e);
        }

        log.info("PDF generation requirements validated for user {} and report type {}.", userId, reportType);
    }

    /**
     * Estimates PDF generation time for the "Profile Summary" report.
     *
     * @param reportType This parameter is now fixed to "PROFILE_SUMMARY".
     * @param userId user ID.
     * @return estimated generation time in seconds.
     */
    public int estimateGenerationTime(String reportType, Long userId) {
        // Since only one report type, fixed estimation
        return 8; // seconds, as it's comprehensive
    }
}