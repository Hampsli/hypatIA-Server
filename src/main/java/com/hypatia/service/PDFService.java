package com.hypatia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypatia.Constants.ReportConstants;
import com.hypatia.dto.*;
import com.hypatia.entity.AiInteraction;
import com.hypatia.entity.User;
import com.hypatia.entity.UserProfile;
import com.hypatia.exception.UserNotFoundException;
import com.hypatia.repository.UserRepository;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.layout.element.ListItem;

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

    @Autowired
    private AIService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // For parsing AIResponseDto content
/*
    @Value("${hypatia.app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${hypatia.organization.name:hypatIA}")
    private String organizationName;

    @Value("${hypatia.report.footer:© 2024 hypatIA. Empowering women in STEM.}")
    private String reportFooter;
*/
    /**
     * Generates a comprehensive "Profile Summary" PDF report for a user,
     * including questionnaire answers and parsed AI analysis results.
     * This method now consolidates all report generation logic into a single type.
     *
     * @param userId The ID of the user for whom the report is generated.     *
     * @return PDF report as ByteArrayResource.
     * @throws UserNotFoundException if user doesn't exist.
     * @throws IOException if PDF generation fails.
     * @throws IllegalArgumentException if reportType is not "PROFILE_SUMMARY" or AI data is malformed.
     */
    public byte[] generateGenericReport(Long userId) throws IOException {
        // Enforce that only "PROFILE_SUMMARY" reports are generated


        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for PDF report generation: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });

        //Map<String, Object> templateData = new HashMap<>();

        ReportData reportData=new ReportData();

        // --- 1. Populate Common Report Data ---
        populateCommonReportData(reportData, user, "Resumen de Perfil Profesional"); // Display name for report type

        // --- 2. Populate User Profile and Assessment Data ---
        populateUserProfileAndAssessmentData(reportData, user);

        //--- 3. populate AI interaction response

        populateAIresponse(reportData,user);

        byte[]report=generateStartPointHypatIA(reportData);
/*
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
            //Map<String, Object> aiContent = aiResponse.getResponseContent();
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
*/
        // --- 5. Generate PDF ---
        log.info("Generating Profile Summary PDF report for user {}", userId);
        return report;//generatePDFFromTemplate("profile-summary-report", templateData); // Use a new specific template name
    }


    /**
     * Populates common data for all PDF reports.
     * @param reportData The map to populate.
     * @param user The user entity.
     * @param reportTypeDisplayName The display name of the report type.
     */
    private void populateCommonReportData(ReportData reportData, User user, String reportTypeDisplayName) {
        CommonReportData commonReportData=new CommonReportData();
        UserDto usrDto=new UserDto();
        usrDto.setId(user.getId());
        usrDto.setRole(user.getRole().name());
        usrDto.setEmail(user.getEmail());
        usrDto.setStatus(user.getStatus().name());

        commonReportData.setUser(usrDto);
        commonReportData.setReportTypeDisplayName(reportTypeDisplayName);
        commonReportData.setFooter(ReportConstants.REPORT_FOOTER);
        commonReportData.setOrganizationName(ReportConstants.ORGANIZATION_NAME);
        commonReportData.setDateTime(LocalDateTime.now());
        reportData.setCommonReportData(commonReportData);
    }

    /**
     * Populates user profile and assessment data for PDF reports.
     * @param reportData The map to populate.
     * @param user The user entity.
     */
    private void populateUserProfileAndAssessmentData(ReportData reportData, User user) {
        UserProfile profile = userService.getUserProfile(user); // Assuming this returns UserProfile or throws if not found
        UserProfileData profileData=new UserProfileData();
        UserProfileDto userProfileDto=userService.toUserProfileDto(profile);
        profileData.setUserProfile(userProfileDto);
        reportData.setProfileData(profileData);
    }

    private void populateAIresponse(ReportData reportData, User user){
        AiInteraction aiInteraction= aiService.getLastInteractionByUserId(user.getId());
        AIResponse aiResponse=new AIResponse();

        try {
            aiResponse=aiService.convertJSON2AiResponse(aiInteraction.getResponsePayload(),AIResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Exception trying to convert aiResponse string to java object"+e.getMessage());
            throw new RuntimeException(e);
        }
        reportData.setAiResponse(aiResponse);
    }

    public byte[] generateStartPointHypatIA(ReportData reportData){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        DateTimeFormatter dtf=DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM yyyy");

        // Título
        document.add(new Paragraph(ReportConstants.REPORT_HEADER_PUNTO_PARTIDA).simulateBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        //introduccin documento
        document.add(new Paragraph(ReportConstants.INTRODUCCION_PUNTO_PARTIDA_1).simulateBold().simulateItalic().setFontSize(14));

        //datos de generacion del doc
        document.add(new Paragraph("Participante: "+reportData.getProfileData().getUserProfile().getName()).setFontSize(12));
        document.add(new Paragraph("Fecha de Generación: "+reportData.getCommonReportData().getDateTime().format(dtf)).setFontSize(12));
        document.add(new Paragraph("Email: "+reportData.getCommonReportData().getDateTime().format(dtf)).setFontSize(12));

        //introduccion
        document.add(new Paragraph(ReportConstants.INTRODUCCION_PUNTO_PARTIDA_2).simulateBold().setFontSize(12));
        document.add(new Paragraph("\n"));
        //perfil del participante
        document.add(new Paragraph(ReportConstants.PERFIL_PARTICIPANTE_HEADER).simulateBold().setFontSize(14));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.SECCION_PERFIL_PARTICIPANTE).simulateBold().setFontSize(14));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.INFORMACION_PERSONAL_HEADER).simulateBold().setFontSize(14));


        com.itextpdf.layout.element.List list = new com.itextpdf.layout.element.List().setSymbolIndent(12).setListSymbol("\u2022 ");
        list.add(new ListItem(ReportConstants.PREGUNTA+" : Género"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getGender()!=null)?(reportData.getProfileData().getUserProfile().getGender()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" : Rango de edad"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getAgeRange()!=null)?(reportData.getProfileData().getUserProfile().getAgeRange()):(ReportConstants.DATO_NO_ENCONTRADO))));
        document.add(list);


        document.add(new Paragraph(ReportConstants.INFORMACION_ESCOLAR_HEADER).simulateBold().setFontSize(14));

        list=new com.itextpdf.layout.element.List();

        list.add(new ListItem(ReportConstants.PREGUNTA+" :  Último grado de estudios completo"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getInitialEducation()!=null)?(reportData.getProfileData().getUserProfile().getInitialEducation()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" : ¿En qué área es tu nivel educativo superior?"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getHigherEducationArea()!=null)?(reportData.getProfileData().getUserProfile().getHigherEducationArea()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" : Si no tienes estudios superiores, ¿Qué tecnología - lenguaje manejas actualmente?"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getTechnologyLanguage()!=null)?(reportData.getProfileData().getUserProfile().getTechnologyLanguage()):(ReportConstants.DATO_NO_ENCONTRADO))));
        document.add(list);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.INFORMACION_COMPLEMENTARIA_LABORAL).simulateBold().setFontSize(14));
        document.add(new Paragraph("\n"));
        list=new com.itextpdf.layout.element.List();

        list.add(new ListItem(ReportConstants.PREGUNTA+" :  ¿Qué posición tienes actualmente?"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getCurrentPosition()!=null)?(reportData.getProfileData().getUserProfile().getCurrentPosition()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" :  ¿Cuántas horas destinadas trabajas a la semana?"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getMaxHoursPerWeek()!=null)?(reportData.getProfileData().getUserProfile().getMaxHoursPerWeek()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" : Tu trabajo es: "));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getWorkMode()!=null)?(reportData.getProfileData().getUserProfile().getWorkMode()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" : Indica tu rango salarial."));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getSalaryRange()!=null)?(reportData.getProfileData().getUserProfile().getSalaryRange()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" : ¿Cuál dirías que es la principal razón para moverte hacia otro espacio laboral?"));
        if (!reportData.getProfileData().getUserProfile().getReasonsForMovement().isEmpty()) {
            for (String reason : reportData.getProfileData().getUserProfile().getReasonsForMovement()) {
                list.add(new ListItem(reason));
            }
        }else{
            list.add(new ListItem(ReportConstants.RESPUESTA+" : "+ReportConstants.DATO_NO_ENCONTRADO));
        }

        list.add(new ListItem(ReportConstants.PREGUNTA+" : : Al cambiarte de empleo, esperarías ganar en promedio cuánto más de lo que percibes actualmente, por mes:"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getExpectedSalary()!=null)?(reportData.getProfileData().getUserProfile().getExpectedSalary()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" :  En los últimos seis meses has completado cursos, talleres, diplomados de cualquier índole"));
        if ((reportData.getProfileData().getUserProfile().getHasCompletedCourses()!=null)) {
            list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getHasCompletedCourses())?("SI"):("NO"))));
        }else {
            list.add(new ListItem(ReportConstants.RESPUESTA+" : "+ReportConstants.DATO_NO_ENCONTRADO));
        }

        list.add(new ListItem(ReportConstants.PREGUNTA+" : ¿Cuántos proyectos has construido con esos cursos, talleres odiplomados? en los últimos dos a tres meses "));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getProjectsBuilt()!=null)?(reportData.getProfileData().getUserProfile().getProjectsBuilt()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" :  Coloca el nombre de tres vacantes a las que te gustaría aplicar actualmente"));
        if (!reportData.getProfileData().getUserProfile().getTargetJobs().isEmpty()) {
            for (String targetJob:reportData.getProfileData().getUserProfile().getTargetJobs()){
                list.add(new ListItem(targetJob));
            }
        }else {
            list.add(new ListItem(ReportConstants.RESPUESTA+" : "+ReportConstants.DATO_NO_ENCONTRADO));
        }

        list.add(new ListItem(ReportConstants.PREGUNTA+" : Enlista un máximo de cinco laborales diarias de manera general (Administrativas y  de desarrollo)"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getDailyTasks()!=null)?(reportData.getProfileData().getUserProfile().getDailyTasks()):(ReportConstants.DATO_NO_ENCONTRADO))));
        document.add(list);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.ACTIVIDADES_DE_CUIDADO).simulateBold().setFontSize(14));
        document.add(new Paragraph("\n"));
        list=new com.itextpdf.layout.element.List();

        list.add(new ListItem(ReportConstants.PREGUNTA+" : ¿Actualmente, ejerces o participas en tareas de cuidado? (de forma directa o indirecta)"));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getCaregiverStatus()!=null)?(reportData.getProfileData().getUserProfile().getCaregiverStatus()):(ReportConstants.DATO_NO_ENCONTRADO))));
        list.add(new ListItem(ReportConstants.PREGUNTA+" :  Si, la respuesta fue que sí participas, ¿Cuánto tiempo dedicas a ello por semana? (O podrías colocar un aproximado de tu tiempo al día en porcentaje). "));
        list.add(new ListItem(ReportConstants.RESPUESTA+" : "+((reportData.getProfileData().getUserProfile().getCaregivingHoursPerWeek()!=null)?(reportData.getProfileData().getUserProfile().getCaregivingHoursPerWeek()):(ReportConstants.DATO_NO_ENCONTRADO))));


        document.add(list);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.ANALISIS_IA_HEADER).simulateBold().setFontSize(16));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.INTRODUCCION_ANALISIS_IA).simulateBold().setFontSize(14));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Retroalimentación analizada:").simulateBold().setFontSize(12));
        document.add(new Paragraph(reportData.getAiResponse().getRetroalimentacion_original()).setFontSize(12));
        Boolean revisionHumanaNecesaria=reportData.getAiResponse().getMetadata_analisis().getRevision_humana_necesaria();
        document.add(new Paragraph("Revisión humana necesaria?: "+((revisionHumanaNecesaria.equals(true))?("SI"):("NO"))).simulateBold().setFontSize(12));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(ReportConstants.RESULTADOS).simulateBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));
        int i=1;
        for (HabilidadesAnalizadas habilidad:reportData.getAiResponse().getHabilidades_analizadas()){
            document.add(new Paragraph("Habilidad: "+i+": "+habilidad.getHabilidad()));
            document.add(new Paragraph("Competencia ligada: "+habilidad.getCompetencia_ligada()));
            document.add(new Paragraph("Nivel de desarrollo: "+habilidad.getNivel_desarrollo()));
            document.add(new Paragraph("Observaciones: "+habilidad.getObservaciones()));
            i++;
        }
        document.add(new Paragraph("Metodologías sugeridas").simulateBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        if (!reportData.getAiResponse().getMetodologias_sugeridas().isEmpty()) {
            for (MetodologiasSugeridas metodologia:reportData.getAiResponse().getMetodologias_sugeridas()){
                document.add(new Paragraph("Metodología sugerida: "+metodologia.getMetodologia()));
                document.add(new Paragraph("Asociada a: "+metodologia.getAsociada_a_habilidad()));
            }

        }else{
            document.add(new Paragraph(ReportConstants.NO_METODOLOGIAS_SUGERIDAS).simulateBold());
        }



        document.close();
        return baos.toByteArray();
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
        status.put("organizationName", null);
        status.put("baseUrl", null);
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