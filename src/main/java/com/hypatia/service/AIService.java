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

    // These values are now directly injected from application properties
    @Value("${hypatia.ai.fastapi.url}")
    private String fastapiBaseUrl;

    @Value("${hypatia.ai.fastapi.analyze-path:/api/analizar}")
    private String fastapiAnalyzePath;

    @Value("${hypatia.ai.request-timeout-seconds:30}")
    private int requestTimeoutSeconds;

    /**
     * Constructor for AIService, autowiring WebClient.Builder and
     * directly injecting FastAPI configuration values.
     * @param webClientBuilder Spring's WebClient.Builder.
     * @param fastapiBaseUrl Base URL of the FastAPI service.
     * @param requestTimeoutSeconds Timeout for requests in seconds.
     */
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
        boolean isCachedResponse = false; // This will always be false when this method is executed (i.e., on a cache miss)

        try {
            Map<String, String> fastapiRequestBody = Map.of("text", inputText);
            requestPayloadForFastAPI = objectMapper.writeValueAsString(fastapiRequestBody);

            responsePayloadFromFastAPI = fastapiWebClient.post()
                    .uri(fastapiAnalyzePath)
                    .bodyValue(requestPayloadForFastAPI)
                    .retrieve()
                    // Use status.value() for direct integer comparison (most compatible across Spring versions)
                    .onStatus(status -> status.value() >= 400 && status.value() < 500, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("FastAPI 4xx client error: {}", body);
                                return new AIServiceException("Error de cliente de FastAPI: " + body + " Estado: " + clientResponse.statusCode().value());
                            }))
                    // Use status.value() for direct integer comparison (most compatible across Spring versions)
                    .onStatus(status -> status.value() >= 500 && status.value() < 600, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("FastAPI 5xx server error: {}", body);
                                return new AIServiceException("Error de servidor de FastAPI: " + body + " Estado: " + clientResponse.statusCode().value());
                            }))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .block();

            // Attempt to deserialize the response content into a Map, then wrap it in AIResponseDto
            AIResponseDto aiResponse = new AIResponseDto(objectMapper.readValue(responsePayloadFromFastAPI, Map.class));

            log.info("Análisis AI de tipo '{}' exitoso (llamada a API) para el el usuario {}. Longitud de respuesta: {} caracteres.", analysisType, userId, responsePayloadFromFastAPI.length());
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
            // Log the interaction with isCachedResponse set to false (because this path means it was an API call)
            logAIInteraction(user, analysisType, requestPayloadForFastAPI, responsePayloadFromFastAPI, cacheKey, false, duration);
        }
    }


    // --- Private Helper Methods ---

    /**
     * Generates a unique SHA-256 hash for caching purposes.
     * This hash is based on the request's core components: user ID, analysis type, and the exact input text.
     * This method must be static for use in @Cacheable's 'key' expression.
     *
     * @param userId The ID of the user.
     * @param analysisType The type of analysis.
     * @param inputText The exact text string sent to FastAPI.
     * @return A SHA-256 hash string representing the unique request.
     */
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
            // Fallback to a less robust hash or throw a runtime exception if key generation is critical
            return String.valueOf(Objects.hash(userId, analysisType, inputText));
        }
    }

    /**
     * Logs the AI interaction to the database.
     *
     * @param user The User entity associated with the interaction.
     * @param interactionType The type of AI analysis requested.
     * @param requestPayload The raw JSON string sent to FastAPI.
     * @param responsePayload The raw JSON string received from FastAPI.
     * @param cacheKey The unique key used for caching this interaction.
     * @param isCachedResponse True if this response was served from cache, false if it was a fresh API call.
     * @param duration The total duration of the AI interaction in milliseconds.
     */
    private void logAIInteraction(User user, String interactionType, String requestPayload,
                                  String responsePayload, String cacheKey, boolean isCachedResponse, long duration) {
        try {
            // Note: `isCachedResponse` here indicates if the *current call path* was a cache hit or miss.
            // When @Cacheable intercepts and serves from cache, this method is not called.
            // When @Cacheable misses, this method is called, and `isCachedResponse` will be false.
            AiInteraction interaction = new AiInteraction(user, interactionType,
                    requestPayload, responsePayload,
                    cacheKey, isCachedResponse); // Store the actual cache status of this interaction
            aiInteractionRepository.save(interaction);
            log.debug("AI interaction logged successfully for user {}: type {}, cached: {}", user.getId(), interactionType, isCachedResponse);
        } catch (Exception e) {
            log.error("Failed to log AI interaction for user {}: {}", user.getId(), e.getMessage(), e);
            // Don't re-throw, as logging failure shouldn't block the main request
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