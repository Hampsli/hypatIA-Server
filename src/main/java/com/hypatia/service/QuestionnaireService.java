package com.hypatia.service;

import com.hypatia.dto.*;
import com.hypatia.entity.*;
import com.hypatia.exception.NotFoundException;
import com.hypatia.repository.CuestionarioRepository;
import com.hypatia.repository.PreguntaRepository;
import com.hypatia.repository.RespuestaUsuarioRepository;
import com.hypatia.repository.UserRepository; // Added import for UserRepository to get User in completion percentage
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionnaireService {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireService.class);

    @Autowired
    private CuestionarioRepository cuestionarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private RespuestaUsuarioRepository respuestaUsuarioRepository;

    @Autowired // Autowire UserRepository to get User entity for percentage calculations
    private UserRepository userRepository;

    // --- MÉTODOS EXISTENTES ---

    @Transactional(readOnly = true)
    public Optional<QuestionnaireDto> getQuestionnaireByPhase(String phase) {
        log.debug("Fetching questionnaire by phase: {}", phase);
        return cuestionarioRepository.findFullQuestionnaireByPhase(phase)
                .map(this::toQuestionnaireDto);
    }

    public void saveUserResponses(User user, List<QuestionnaireResponseDto> responses) {
        String sessionId = UUID.randomUUID().toString();
        log.info("Saving {} user responses for user {} with new session ID: {}", responses.size(), user.getId(), sessionId);

        List<Long> questionIds = responses.stream()
                .map(QuestionnaireResponseDto::getQuestionId)
                .collect(Collectors.toList());
        if (!questionIds.isEmpty()) {
            respuestaUsuarioRepository.markResponsesAsNotCurrentForQuestionIds(user.getId(), questionIds);
            log.debug("Marked previous responses as not current for user {} and questions {}.", user.getId(), questionIds);
        }

        for (QuestionnaireResponseDto resDto : responses) {
            Pregunta pregunta = preguntaRepository.findById(resDto.getQuestionId())
                    .orElseThrow(() -> {
                        log.warn("Pregunta no encontrada con ID: {}", resDto.getQuestionId());
                        return new NotFoundException("Pregunta no encontrada con ID: " + resDto.getQuestionId());
                    });

            RespuestaUsuario nuevaRespuesta = new RespuestaUsuario();
            nuevaRespuesta.setUser(user);
            nuevaRespuesta.setPregunta(pregunta);
            nuevaRespuesta.setRespuestaTexto(resDto.getAnswer());
            nuevaRespuesta.setSessionId(sessionId);
            nuevaRespuesta.setCurrent(true);
            respuestaUsuarioRepository.save(nuevaRespuesta);
            log.debug("Saved response for question {}: {}", resDto.getQuestionId(), resDto.getAnswer());
        }
        log.info("User responses saved successfully for user {}.", user.getId());
    }

    @Transactional(readOnly = true)
    public double getCompletionPercentage(User user, String phase) {
        Long totalPreguntas = preguntaRepository.countByCuestionario_FaseCuestionario(phase);
        if (totalPreguntas == 0) {
            log.debug("No questions found for phase {}. Completion is 100%.", phase);
            return 100.0;
        }
        long respuestasUsuario = respuestaUsuarioRepository
                .countByUser_IdAndPregunta_Cuestionario_FaseCuestionarioAndIsCurrentTrue(user.getId(), phase);
        double percentage = ((double) respuestasUsuario / totalPreguntas) * 100.0;
        log.debug("Completion for user {} in phase {}: {}%", user.getId(), phase, percentage);
        return percentage;
    }

    public long resetUserResponses(User user, String phase) {
        log.info("Resetting user responses for user {} in phase {}.", user.getId(), phase);
        long updatedCount = respuestaUsuarioRepository.markAllResponsesAsNotCurrent(user.getId(), phase);
        log.info("Marked {} responses as not current for user {} in phase {}.", updatedCount, user.getId(), phase);
        return updatedCount;
    }

    // --- MÉTODOS NUEVOS AÑADIDOS/OPTIMIZADOS ---

    @Transactional(readOnly = true)
    public String getQuestionnairePhaseFromQuestionId(Long questionId) {
        String phase = preguntaRepository.findFaseCuestionarioById(questionId);
        if (phase == null) {
            log.warn("Questionnaire phase not found for question ID: {}", questionId);
            throw new NotFoundException("No se pudo encontrar un cuestionario para la pregunta con ID: " + questionId);
        }
        log.debug("Found phase '{}' for question ID: {}", phase, questionId);
        return phase;
    }

    @Transactional(readOnly = true)
    public List<RespuestaUsuarioDto> getUserResponses(User user, String phase) {
        log.debug("Fetching user responses for user {} in phase {}.", user.getId(), phase);
        List<RespuestaUsuario> responses = respuestaUsuarioRepository.findCurrentUserResponsesByPhase(user.getId(), phase);
        List<RespuestaUsuarioDto> dtos = responses.stream()
                .map(this::toRespuestaUsuarioDto)
                .collect(Collectors.toList());
        log.debug("Found {} current responses for user {} in phase {}.", dtos.size(), user.getId(), phase);
        return dtos;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProgress(User user, String phase) {
        long totalPreguntas = preguntaRepository.countByCuestionario_FaseCuestionario(phase);
        long respuestasUsuario = respuestaUsuarioRepository.countByUser_IdAndPregunta_Cuestionario_FaseCuestionarioAndIsCurrentTrue(user.getId(), phase);
        double completionPercentage = (totalPreguntas == 0) ? 100.0 : ((double) respuestasUsuario / totalPreguntas) * 100.0;

        Map<String, Object> progress = Map.of(
                "completionPercentage", completionPercentage,
                "isCompleted", completionPercentage >= 100.0,
                "totalQuestions", totalPreguntas,
                "answeredQuestions", respuestasUsuario,
                "remainingQuestions", totalPreguntas - respuestasUsuario
        );
        log.debug("Progress for user {} in phase {}: {}", user.getId(), phase, progress);
        return progress;
    }

    /**
     * Gets all current questions and user's answers for a given user,
     * formatted for report generation.
     * Includes both the question text and the selected option text (if applicable).
     *
     * @param user The User entity.
     * @return A List of Maps, each representing a question and its current answer.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserQuestionsAndAnswers(User user) { // Changed parameter to User object
        log.debug("Fetching all current questions and answers for user: {}", user.getEmail());

        List<RespuestaUsuario> responses = respuestaUsuarioRepository.findCurrentUserResponsesWithDetails(user.getId());

        if (responses.isEmpty()) {
            log.info("No current responses found for user ID: {}", user.getId());
            return Collections.emptyList();
        }

        List<Map<String, Object>> qAndAData = new ArrayList<>();

        for (RespuestaUsuario response : responses) {
            Map<String, Object> qaMap = new LinkedHashMap<>();
            Pregunta pregunta = response.getPregunta();

            qaMap.put("questionId", pregunta.getId());
            qaMap.put("questionText", pregunta.getTextoPregunta());
            qaMap.put("questionType", pregunta.getTipoPregunta());
            qaMap.put("userAnswer", response.getRespuestaTexto());

            if (pregunta.getTipoPregunta().startsWith("selector_") && pregunta.getOpciones() != null) {
                Optional<OpcionPregunta> selectedOption = pregunta.getOpciones().stream()
                        .filter(op -> op.getValorOpcion() != null && op.getValorOpcion().equals(response.getRespuestaTexto()))
                        .findFirst();

                if (selectedOption.isPresent()) {
                    qaMap.put("selectedOptionText", selectedOption.get().getTextoOpcion());
                } else {
                    qaMap.put("selectedOptionText", response.getRespuestaTexto());
                    log.warn("Selected option text not found for question {} (type {}) with answer '{}' for user {}. Using raw answer.",
                            pregunta.getId(), pregunta.getTipoPregunta(), response.getRespuestaTexto(), user.getId());
                }
            } else {
                qaMap.put("selectedOptionText", response.getRespuestaTexto());
            }

            qAndAData.add(qaMap);
        }
        log.debug("Generated {} question/answer pairs for user {}.", qAndAData.size(), user.getEmail());
        return qAndAData;
    }


    /**
     * Prepares user assessment data for AI analysis.
     *
     * @param user The User entity.
     * @return Map containing assessment data.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> prepareDataForAIAnalysis(User user) { // Changed parameter to User object
        log.debug("Preparing assessment data for AI analysis for user: {}", user.getEmail());
        Map<String, Object> data = new HashMap<>();

        // Get questions and answers (now takes User object)
        List<Map<String, Object>> qAndA = getUserQuestionsAndAnswers(user);
        data.put("questions_and_answers", qAndA);

        // Calculate category scores (now takes User object)
        Map<String, Double> categoryScores = calculateCategoryScores(user);
        data.put("category_scores", categoryScores);

        // Add completion percentage (now takes User object)
        double completion = getCompletionPercentage(user, "onboarding");
        data.put("completion_percentage", completion);

        // Add any other relevant summary data from assessment (now takes User object)
        Optional<Map<String, Object>> summary = getAssessmentSummary(user);
        summary.ifPresent(s -> data.put("assessment_summary", s));

        log.debug("Prepared assessment data for AI: {} keys for user {}.", data.size(), user.getEmail());
        return data;
    }

    /**
     * Calculates category-wise scores for user's assessment.
     *
     * @param user The User entity.
     * @return Map of category names to their scores.
     */
    @Transactional(readOnly = true)
    public Map<String, Double> calculateCategoryScores(User user) { // Changed parameter to User object
        log.debug("Calculating category scores for user: {}", user.getEmail());
        // This is a placeholder. Real implementation would involve:
        // 1. Fetching current user responses for `user`.
        // 2. Mapping responses to specific categories/skills.
        // 3. Calculating scores based on predefined rules or rubrics.
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Analisis de Datos", 85.0);
        scores.put("Comunicacion Efectiva", 70.0);
        scores.put("Liderazgo", 60.0);
        scores.put("Resolucion de Problemas", 90.0);
        scores.put("Colaboracion", 75.0);
        log.debug("Calculated dummy category scores for user {}.", user.getEmail());
        return scores;
    }

    /**
     * Gets a summary of the user's assessment, potentially for a dashboard or report.
     *
     * @param user The User entity.
     * @return Optional Map containing assessment summary.
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getAssessmentSummary(User user) {
        log.debug("Getting assessment summary for user: {}", user.getEmail());
        // This is a placeholder. Real implementation would aggregate summary info for `user`.
        Map<String, Object> summary = new HashMap<>();
        summary.put("status", "Completed");
        summary.put("lastCompletedDate", LocalDateTime.now().minusDays(5));
        summary.put("totalCategories", 5);
        log.debug("Generated dummy assessment summary for user {}.", user.getEmail());
        return Optional.of(summary);
    }


    /**
     * Gets the overall assessment completion percentage for a user.
     *
     * @param user The User entity.
     * @return Double representing completion percentage.
     */
    @Transactional(readOnly = true)
    public Double getAssessmentCompletionPercentage(User user) { // Changed parameter to User object
        return getCompletionPercentage(user, "onboarding");
    }

    // --- MÉTODOS DE MAPEO (Entity a DTO) ---

    private QuestionnaireDto toQuestionnaireDto(Cuestionario entity) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setId(entity.getId());
        dto.setName(entity.getNombre());
        dto.setDescription(entity.getDescripcion());
        dto.setPhase(entity.getFaseCuestionario());
        if (entity.getSecciones() != null) {
            dto.setSections(entity.getSecciones().stream()
                    .map(this::toSectionDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setSections(Collections.emptyList());
        }
        return dto;
    }

    private SectionDto toSectionDto(CuestionarioSeccion entity) {
        SectionDto dto = new SectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getNombreSeccion());
        dto.setOrder(entity.getOrden());
        if (entity.getPreguntas() != null) {
            dto.setQuestions(entity.getPreguntas().stream()
                    .map(this::toQuestionDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setQuestions(Collections.emptyList());
        }
        return dto;
    }

    private QuestionDto toQuestionDto(Pregunta entity) {
        QuestionDto dto = new QuestionDto();
        dto.setId(entity.getId());
        dto.setText(entity.getTextoPregunta());
        dto.setHelpText(entity.getTextoAyuda());
        dto.setType(entity.getTipoPregunta());
        dto.setOrder(entity.getOrden());
        if (entity.getOpciones() != null) {
            dto.setOptions(entity.getOpciones().stream()
                    .map(this::toOptionDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setOptions(Collections.emptyList());
        }
        return dto;
    }

    private OptionDto toOptionDto(OpcionPregunta entity) {
        OptionDto dto = new OptionDto();
        dto.setId(entity.getId());
        dto.setText(entity.getTextoOpcion());
        dto.setValue(entity.getValorOpcion());
        dto.setOrder(entity.getOrden());
        return dto;
    }

    private RespuestaUsuarioDto toRespuestaUsuarioDto(RespuestaUsuario entity) {
        RespuestaUsuarioDto dto = new RespuestaUsuarioDto();
        dto.setQuestionId(entity.getPregunta().getId());
        dto.setQuestionText(entity.getPregunta().getTextoPregunta());
        dto.setAnswer(entity.getRespuestaTexto());
        return dto;
    }
}