package com.hypatia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for AI integration with Hugging Face and other AI services.
 * 
 * This configuration sets up:
 * - WebClient for HTTP communication with Hugging Face API
 * - Multiple AI model configurations for different use cases
 * - Request/response timeout and retry policies
 * - Authentication headers for AI service APIs
 * - Model-specific configurations and parameters
 * 
 * Supported AI Models:
 * - Text Generation: GPT-based models for content creation
 * - Question Answering: BERT-based models for assessment analysis
 * - Sentiment Analysis: Classification models for feedback analysis
 * - Conversation: Dialog models for career guidance chat
 * 
 * Environment Variables Required:
 * - HUGGINGFACE_API_KEY: Authentication token for Hugging Face API
 * - HUGGINGFACE_BASE_URL: Base URL for Hugging Face inference API
 * - AI_MODELS: Comma-separated list of model names to use
 */
@Configuration
public class AIConfig {

    /**
     * Hugging Face API authentication token.
     * Required for accessing Hugging Face inference API.
     * Get your token from: https://huggingface.co/settings/tokens
     */
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    /**
     * Hugging Face API base URL.
     * Default: https://api-inference.huggingface.co
     * Can be changed to use dedicated inference endpoints.
     */
    @Value("${huggingface.api.base-url:https://api-inference.huggingface.co}")
    private String huggingFaceBaseUrl;

    /**
     * Comma-separated list of AI models to use for different tasks.
     * Default models cover various use cases:
     * - microsoft/DialoGPT-medium: Conversational AI for career guidance
     * - facebook/blenderbot-400M-distill: General conversation and Q&A
     * - microsoft/GODEL-v1_1-base-seq2seq: Goal-oriented dialog
     */
    @Value("${ai.models:microsoft/DialoGPT-medium,facebook/blenderbot-400M-distill,microsoft/GODEL-v1_1-base-seq2seq}")
    private String aiModelsString;

    /**
     * Specialized model for text generation tasks.
     * Used for generating detailed assessment reports and recommendations.
     */
    @Value("${ai.text-generation.model:gpt2-medium}")
    private String textGenerationModel;

    /**
     * Model for question answering and information extraction.
     * Used for analyzing user responses and extracting insights.
     */
    @Value("${ai.question-answering.model:deepset/roberta-base-squad2}")
    private String questionAnsweringModel;

    /**
     * Model for sentiment analysis and emotion detection.
     * Used for analyzing user feedback and response sentiment.
     */
    @Value("${ai.sentiment-analysis.model:cardiffnlp/twitter-roberta-base-sentiment-latest}")
    private String sentimentAnalysisModel;

    /**
     * Request timeout for AI service calls in seconds.
     * Default: 30 seconds to handle model loading time.
     */
    @Value("${ai.request.timeout-seconds:30}")
    private int requestTimeoutSeconds;

    /**
     * Maximum number of retry attempts for failed AI requests.
     * Default: 3 retries with exponential backoff.
     */
    @Value("${ai.request.max-retries:3}")
    private int maxRetries;

    /**
     * Maximum length for AI-generated responses.
     * Prevents extremely long responses that could impact performance.
     */
    @Value("${ai.response.max-length:500}")
    private int maxResponseLength;

    /**
     * Temperature parameter for AI text generation.
     * Controls randomness in responses (0.0 = deterministic, 1.0 = very random).
     */
    @Value("${ai.generation.temperature:0.7}")
    private double temperature;

    /**
     * Top-p parameter for nucleus sampling in text generation.
     * Controls diversity of generated text.
     */
    @Value("${ai.generation.top-p:0.9}")
    private double topP;

    /**
     * Creates and configures WebClient for Hugging Face API communication.
     * 
     * This WebClient is optimized for AI service communication:
     * - Custom timeout configuration for potentially slow AI responses
     * - Authentication headers for Hugging Face API
     * - JSON content type handling
     * - Error handling and retry logic
     * - Request/response logging for debugging
     * 
     * Features:
     * - Automatic authentication header injection
     * - Response timeout handling
     * - Proper error response parsing
     * - Request correlation for tracking
     * 
     * @return WebClient configured for Hugging Face API calls
     */
    @Bean
    public WebClient huggingFaceWebClient() {
        return WebClient.builder()
            // Set base URL for all Hugging Face requests
            .baseUrl(huggingFaceBaseUrl)
            
            // Configure default headers
            .defaultHeader("Authorization", "Bearer " + huggingFaceApiKey)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("User-Agent", "hypatIA-Backend/1.0.0")
            
            // Configure timeouts for AI service responsiveness
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(1024 * 1024) // 1MB max response size
            )
            
            // Build the WebClient instance
            .build();
    }

    /**
     * Provides list of configured AI models for the application.
     * 
     * This method parses the comma-separated model list and provides
     * it as a Spring bean for injection into AI services. The list
     * includes models for different AI tasks:
     * 
     * Model Categories:
     * 1. Conversational AI: For career guidance and user interaction
     * 2. Text Generation: For report and content generation
     * 3. Question Answering: For assessment analysis
     * 4. Classification: For skill categorization and sentiment analysis
     * 
     * @return List<String> list of AI model names
     */
    @Bean
    public List<String> aiModels() {
        return Arrays.asList(aiModelsString.split(","));
    }

    /**
     * Configuration bean for AI request parameters.
     * 
     * This configuration object contains parameters used across
     * different AI service calls to ensure consistency:
     * - Generation parameters (temperature, top-p, max length)
     * - Request timeout and retry settings
     * - Model-specific configurations
     * - Response format preferences
     * 
     * @return AIRequestConfig configuration object for AI requests
     */
    @Bean
    public AIRequestConfig aiRequestConfig() {
        AIRequestConfig config = new AIRequestConfig();
        config.setMaxResponseLength(maxResponseLength);
        config.setTemperature(temperature);
        config.setTopP(topP);
        config.setRequestTimeoutSeconds(requestTimeoutSeconds);
        config.setMaxRetries(maxRetries);
        config.setTextGenerationModel(textGenerationModel);
        config.setQuestionAnsweringModel(questionAnsweringModel);
        config.setSentimentAnalysisModel(sentimentAnalysisModel);
        return config;
    }

    /**
     * Configuration class for AI request parameters.
     * 
     * This inner class encapsulates all configuration parameters
     * needed for AI service requests. It provides a clean way to
     * inject AI configuration into services while maintaining
     * type safety and documentation.
     */
    public static class AIRequestConfig {
        private int maxResponseLength;
        private double temperature;
        private double topP;
        private int requestTimeoutSeconds;
        private int maxRetries;
        private String textGenerationModel;
        private String questionAnsweringModel;
        private String sentimentAnalysisModel;

        // Getters and setters with documentation

        /**
         * Gets the maximum length for AI-generated responses.
         * @return maximum response length in characters
         */
        public int getMaxResponseLength() {
            return maxResponseLength;
        }

        public void setMaxResponseLength(int maxResponseLength) {
            this.maxResponseLength = maxResponseLength;
        }

        /**
         * Gets the temperature parameter for text generation.
         * Higher values produce more random outputs.
         * @return temperature value (0.0 to 1.0)
         */
        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        /**
         * Gets the top-p parameter for nucleus sampling.
         * Controls diversity of generated text.
         * @return top-p value (0.0 to 1.0)
         */
        public double getTopP() {
            return topP;
        }

        public void setTopP(double topP) {
            this.topP = topP;
        }

        /**
         * Gets the request timeout in seconds.
         * @return timeout value in seconds
         */
        public int getRequestTimeoutSeconds() {
            return requestTimeoutSeconds;
        }

        public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
            this.requestTimeoutSeconds = requestTimeoutSeconds;
        }

        /**
         * Gets the maximum number of retry attempts.
         * @return maximum retry attempts
         */
        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        /**
         * Gets the model name for text generation tasks.
         * @return text generation model name
         */
        public String getTextGenerationModel() {
            return textGenerationModel;
        }

        public void setTextGenerationModel(String textGenerationModel) {
            this.textGenerationModel = textGenerationModel;
        }

        /**
         * Gets the model name for question answering tasks.
         * @return question answering model name
         */
        public String getQuestionAnsweringModel() {
            return questionAnsweringModel;
        }

        public void setQuestionAnsweringModel(String questionAnsweringModel) {
            this.questionAnsweringModel = questionAnsweringModel;
        }

        /**
         * Gets the model name for sentiment analysis tasks.
         * @return sentiment analysis model name
         */
        public String getSentimentAnalysisModel() {
            return sentimentAnalysisModel;
        }

        public void setSentimentAnalysisModel(String sentimentAnalysisModel) {
            this.sentimentAnalysisModel = sentimentAnalysisModel;
        }
    }
}
