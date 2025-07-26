package com.hypatia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
// Removed unused imports: Arrays, List

/**
 * Configuration class for AI integration with a generic FastAPI backend.
 *
 * This configuration sets up:
 * - WebClient for HTTP communication with the FastAPI AI service.
 * - Global request timeout for AI service calls.
 * - Common headers for FastAPI communication.
 *
 * Environment Variables Required:
 * - HYPATIA_AI_FASTAPI_URL: Base URL for your FastAPI AI service.
 * - HYPATIA_AI_REQUEST_TIMEOUT_SECONDS: Timeout for AI requests.
 *
 * Note: Model-specific configurations and parameters are now managed
 * within the FastAPI service itself.
 */
@Configuration
public class AIConfig {

    /**
     * Base URL for your FastAPI AI service (e.g., http://localhost:8000).
     * This is the entry point for all AI requests from the Spring backend.
     */
    @Value("${hypatia.ai.fastapi.url}")
    private String fastapiBaseUrl;

    /**
     * Request timeout for AI service calls in seconds.
     * Default: 30 seconds to accommodate potential AI model loading times.
     */
    @Value("${hypatia.ai.request-timeout-seconds:30}")
    private int requestTimeoutSeconds;

    // --- REMOVED PROPERTIES (NO LONGER RELEVANT FOR FastAPI PROXY ARCHITECTURE) ---
    // - huggingFaceApiKey
    // - huggingFaceBaseUrl (replaced by fastapiBaseUrl)
    // - aiModelsString (FastAPI manages internal models)
    // - textGenerationModel (FastAPI manages internal models)
    // - questionAnsweringModel (FastAPI manages internal models)
    // - sentimentAnalysisModel (FastAPI manages internal models)
    // - maxRetries (WebClient or higher-level logic should handle, not specific to AI models now)
    // - maxResponseLength (FastAPI handles its own response generation parameters)
    // - temperature (FastAPI handles its own generation parameters)
    // - topP (FastAPI handles its own generation parameters)

    /**
     * Creates and configures WebClient for FastAPI AI service communication.
     *
     * This WebClient is optimized for generic AI service communication:
     * - Uses the configured FastAPI base URL.
     * - Sets common headers (like Content-Type).
     * - Configures a global request timeout.
     *
     * Note: Authentication headers for FastAPI should be configured here if your FastAPI
     * requires an API key for authentication.
     *
     * @return WebClient configured for FastAPI API calls.
     */
    @Bean
    public WebClient fastapiWebClient() {
        return WebClient.builder()
                // Set base URL for all FastAPI requests
                .baseUrl(fastapiBaseUrl)

                // Configure common headers (FastAPI typically expects JSON)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "hypatIA-SpringBackend/1.0.0") // Identify your application

                // Optional: If FastAPI requires an API key in header (e.g., "X-API-Key")
                // .defaultHeader("X-API-Key", "your-fastapi-api-key-here-from-properties")

                // Configure timeouts for AI service responsiveness
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024) // 1MB max response size
                )
                // Timeout applied in AIService method via .timeout(Duration.ofSeconds(...))
                // .build() is called directly in AIService constructor now
                .build(); // Build the WebClient instance
    }

}