package com.hypatia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypatia.dto.AIResponseDto;
import com.hypatia.entity.AiInteraction;
import com.hypatia.entity.User;
import com.hypatia.exception.AIServiceException;
import com.hypatia.exception.UserNotFoundException;
import com.hypatia.repository.AiInteractionRepository;
import com.hypatia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.http.HttpStatus; // Keep this import for HttpStatus enum constants
import org.springframework.http.HttpStatusCode; // Keep this import for the predicate type
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for AI integration, acting as a proxy to a generic FastAPI AI backend.
 * Now optimized for direct text input from the frontend and includes Spring-side caching.
 */
@Service
@Transactional
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final WebClient fastapiWebClient;

    @Autowired
    private AiInteractionRepository aiInteractionRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hypatia.ai.fastapi.url}")
    private String fastapiBaseUrl;

    @Value("${hypatia.ai.fastapi.analyze-path:/api/analizar}")
    private String fastapiAnalyzePath;

    @Value("${hypatia.ai.request-timeout-seconds:30}")
    private int requestTimeoutSeconds;

    @Autowired
    public AIService(WebClient.Builder webClientBuilder,
                     @Value("${hypatia.ai.fastapi.url}") String fastapiBaseUrl,
                     @Value("${hypatia.ai.request-timeout-seconds:30}") int requestTimeoutSeconds) {
        this.fastapiBaseUrl = fastapiBaseUrl;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
        this.fastapiWebClient = webClientBuilder.baseUrl(fastapiBaseUrl)
                .defaultHeaders(header -> header.add("Content-Type", "application/json"))
                .build();
    }

    /**
     * Retrieves a generic AI analysis from FastAPI.
     * The input 'text' comes directly from the frontend.
     * This method is CACHEABLE based on user, analysis type, and the exact text sent.
     *
     * @param userId The ID of the user for whom the analysis is requested.
     * @param analysisType The specific type of analysis requested (e.g., "ASSESSMENT_ANALYSIS", "CHAT").
     * @param inputText The exact text string from the frontend to be sent to FastAPI.
     * @return AIResponseDto containing the generic JSON response from FastAPI.
     * @throws UserNotFoundException if the user doesn't exist.
     * @throws AIServiceException if the call to the FastAPI service fails or returns an error.
     */
    @Cacheable(value = "aiResponses", key = "#userId + ':' + #analysisType + ':' + T(com.hypatia.service.AIService).generateHash(#inputText)")
    public AIResponseDto getGenericAIAnalysis(Long userId, String analysisType, String inputText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        long startTime = System.currentTimeMillis();
        String requestPayloadForFastAPI = null;
        String responsePayloadFromFastAPI = null;
        boolean isCachedResponse = false; // Always false when this method runs (cache miss)

        try {
            Map<String, String> fastapiRequestBody = Map.of("text", inputText);
            requestPayloadForFastAPI = objectMapper.writeValueAsString(fastapiRequestBody);

            responsePayloadFromFastAPI = fastapiWebClient.post()
                    .uri(fastapiAnalyzePath)
                    .bodyValue(requestPayloadForFastAPI)
                    .retrieve()
                    // FIX: Use a lambda with getRawStatusCode() for compatibility across Spring versions
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("FastAPI 4xx client error: {}", body);
                                return new AIServiceException("Error de cliente de FastAPI: " + body + " Estado: " + clientResponse.statusCode().value()); // Use .value() for int
                            }))
                    // FIX: Use a lambda with getRawStatusCode() for compatibility across Spring versions
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("FastAPI 5xx server error: {}", body);
                                return new AIServiceException("Error de servidor de FastAPI: " + body + " Estado: " + clientResponse.statusCode().value()); // Use .value() for int
                            }))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .block();

            AIResponseDto aiResponse = new AIResponseDto(objectMapper.readValue(responsePayloadFromFastAPI, Map.class));

            log.info("Análisis AI de tipo '{}' exitoso (llamada a API) para el usuario {}. Longitud de respuesta: {} caracteres.", analysisType, userId, responsePayloadFromFastAPI.length());
            return aiResponse;

        } catch (JsonProcessingException | WebClientException e) {
            log.error("Fallo al comunicarse con FastAPI o al procesar JSON para el usuario {}: {}", userId, e.getMessage(), e);
            throw new AIServiceException("Error de comunicación con el servicio de IA. Inténtalo de nuevo más tarde.", e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado en el servicio de IA para el usuario {}: {}", userId, e.getMessage(), e);
            throw new AIServiceException("Error inesperado al procesar la solicitud de IA: " + e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String cacheKey = generateHash(userId, analysisType, inputText);
            logAIInteraction(user, analysisType, requestPayloadForFastAPI, responsePayloadFromFastAPI, cacheKey, isCachedResponse, duration);
        }
    }


    // --- Private Helper Methods ---

    public static String generateHash(Long userId, String analysisType, String inputText) {
        try {
            String rawKey = userId + ":" + analysisType + ":" + inputText;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating cache key hash (SHA-256 not available): {}", e.getMessage(), e);
            return String.valueOf(Objects.hash(userId, analysisType, inputText));
        }
    }

    private void logAIInteraction(User user, String interactionType, String requestPayload,
                                  String responsePayload, String cacheKey, boolean isCachedResponse, long duration) {
        try {
            AiInteraction interaction = new AiInteraction(user, interactionType,
                    requestPayload, responsePayload,
                    cacheKey, isCachedResponse);
            aiInteractionRepository.save(interaction);
            log.debug("AI interaction logged successfully for user {}: type {}, cached: {}", user.getId(), interactionType, isCachedResponse);
        } catch (Exception e) {
            log.error("Failed to log AI interaction for user {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AiInteraction> getUserAIHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return aiInteractionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAIServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInteracciones", aiInteractionRepository.count());
        stats.put("interaccionesCache", aiInteractionRepository.countByIsCachedResponseTrue());
        stats.put("interaccionesAPI", aiInteractionRepository.countByIsCachedResponseFalse());
        long totalInteractions = aiInteractionRepository.count();
        long cachedInteractions = aiInteractionRepository.countByIsCachedResponseTrue();
        double cacheHitRate = totalInteractions > 0 ? (double) cachedInteractions / totalInteractions * 100 : 0;
        stats.put("tasaAciertoCache", Math.round(cacheHitRate * 100.0) / 100.0);
        List<Object[]> typeCounts = aiInteractionRepository.countInteractionsByType();
        Map<String, Long> interactionTypeStats = new HashMap<>();
        for (Object[] row : typeCounts) {
            interactionTypeStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("estadisticasPorTipoInteraccion", interactionTypeStats);
        return stats;
    }

    @Transactional
    public int cleanupOldInteractions(int olderThanDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(olderThanDays);
        int cleanedUpCount = aiInteractionRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Cleaned up {} AI interactions older than {} days.", cleanedUpCount, olderThanDays);
        return cleanedUpCount;
    }
}