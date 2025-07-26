package com.hypatia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Hugging Face AI integration configuration.
 * 
 * This configuration sets up AI capabilities for:
 * - Assessment analysis and personalized insights
 * - Career recommendation generation
 * - Skills gap analysis and learning suggestions
 * - Natural language processing for user inputs
 * - Multiple AI model support for different use cases
 * 
 * Supported AI Models:
 * 1. Text Generation: For creating personalized assessment reports
 * 2. Sentiment Analysis: For analyzing user feedback and responses
 * 3. Text Classification: For categorizing skills and career paths
 * 4. Question Answering: For AI-powered career guidance
 * 5. Summarization: For condensing assessment results
 * 
 * API Integration Features:
 * - Rate limiting and retry mechanisms
 * - Response caching for cost optimization
 * - Multiple model fallback strategies
 * - Request/response logging for analytics
 * - Error handling and graceful degradation
 * 
 * Environment Variables Required:
 * - HUGGINGFACE_API_KEY: API authentication token
 * - HUGGINGFACE_BASE_URL: API base URL (optional, defaults to official)
 * 
 * Cost Optimization:
 * - Response caching reduces API calls
 * - Request batching for efficiency
 * - Model selection based on complexity needs
 * - Usage monitoring and alerting
 * 
 * Required Libraries:
 * - spring-boot-starter-webflux (for WebClient)
 * - jackson-databind (for JSON processing)
 * 
 * @author hypatIA Development Team
 */
@Configuration
public class HuggingFaceConfig {

    /**
     * Hugging Face API key for authentication.
     * 
     * API Key Setup:
     * 1. Create account at https://huggingface.co/
     * 2. Generate API key in account settings
     * 3. Set as environment variable: HUGGINGFACE_API_KEY
     * 
     * Security Best Practices:
     * - Never commit API keys to version control
     * - Use environment variables or secure vaults
     * - Rotate keys regularly for security
     * - Monitor API usage for unexpected activity
     */
    @Value("${huggingface.api.key:}")
    private String apiKey;

    /**
     * Hugging Face API base URL.
     * Default: https://api-inference.huggingface.co
     * 
     * Alternative endpoints:
     * - Dedicated inference endpoints for production
     * - Custom model deployments
     * - Regional endpoints for latency optimization
     */
    @Value("${huggingface.api.base-url:https://api-inference.huggingface.co}")
    private String baseUrl;

    /**
     * API request timeout in milliseconds.
     * Default: 30 seconds for AI model processing
     * 
     * Timeout Considerations:
     * - AI models can take time to process requests
     * - Larger models require longer processing time
     * - Network latency and model loading time
     * - Balance between user experience and reliability
     */
    @Value("${huggingface.api.timeout:30000}")
    private int timeoutMs;

    /**
     * Number of retry attempts for failed requests.
     * Default: 3 retries with exponential backoff
     * 
     * Retry Strategy:
     * - Retry on network errors and 5xx responses
     * - Exponential backoff to avoid overwhelming API
     * - Maximum retry limit to prevent infinite loops
     * - Different retry logic for different error types
     */
    @Value("${huggingface.api.retry-attempts:3}")
    private int retryAttempts;

    /**
     * WebClient configuration for Hugging Face API calls.
     * 
     * WebClient Features:
     * - Reactive, non-blocking HTTP client
     * - Built-in support for JSON processing
     * - Configurable timeouts and retry logic
     * - Request/response interceptors for logging
     * - Connection pooling and keep-alive
     * 
     * Configuration Details:
     * - Base URL set to Hugging Face API endpoint
     * - Authorization header with API key
     * - Content-Type header for JSON requests
     * - Timeout configuration for long-running AI requests
     * - User-Agent for API usage identification
     * 
     * @return WebClient configured for Hugging Face API
     */
    @Bean("huggingFaceWebClient")
    public WebClient huggingFaceWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "hypatIA-backend/1.0.0")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB max response size
                .build();
    }

    /**
     * AI model configuration mapping.
     * 
     * This configuration maps different AI use cases to specific
     * Hugging Face models optimized for each task:
     * 
     * Text Generation Models:
     * - gpt2: General purpose text generation
     * - microsoft/DialoGPT-medium: Conversational AI
     * - EleutherAI/gpt-neo-2.7B: Advanced text generation
     * 
     * Sentiment Analysis Models:
     * - cardiffnlp/twitter-roberta-base-sentiment-latest: Social media sentiment
     * - nlptown/bert-base-multilingual-uncased-sentiment: Multilingual sentiment
     * 
     * Text Classification Models:
     * - facebook/bart-large-mnli: Natural language inference
     * - microsoft/DialoGPT-medium: Intent classification
     * 
     * Question Answering Models:
     * - deepset/roberta-base-squad2: Extractive QA
     * - microsoft/DialoGPT-medium: Generative QA
     * 
     * @return Map of model types to model names
     */
    @Bean("aiModelConfig")
    public Map<String, String> aiModelConfig() {
        return Map.of(
            // Text generation for assessment reports and recommendations
            "text-generation", "gpt2",
            
            // Sentiment analysis for user feedback evaluation
            "sentiment-analysis", "cardiffnlp/twitter-roberta-base-sentiment-latest",
            
            // Text classification for skills and career categorization
            "text-classification", "facebook/bart-large-mnli",
            
            // Question answering for career guidance
            "question-answering", "deepset/roberta-base-squad2",
            
            // Summarization for condensing assessment results
            "summarization", "facebook/bart-large-cnn",
            
            // Feature extraction for skills matching
            "feature-extraction", "sentence-transformers/all-MiniLM-L6-v2"
        );
    }

    /**
     * AI request configuration for optimization.
     * 
     * Configuration Parameters:
     * - max_length: Maximum response length for text generation
     * - temperature: Creativity level (0.0 = deterministic, 1.0 = creative)
     * - top_p: Nucleus sampling parameter for response diversity
     * - repetition_penalty: Penalty for repetitive text
     * - wait_for_model: Wait if model is loading (true for better reliability)
     * 
     * Use Case Optimization:
     * - Assessment reports: Lower temperature for consistency
     * - Career suggestions: Higher temperature for creativity
     * - Skills analysis: Balanced settings for accuracy
     * 
     * @return Map of AI request parameters
     */
    @Bean("aiRequestConfig")
    public Map<String, Object> aiRequestConfig() {
        return Map.of(
            // Text generation parameters
            "max_length", 500,
            "temperature", 0.7,
            "top_p", 0.9,
            "repetition_penalty", 1.1,
            "wait_for_model", true,
            
            // Request optimization
            "use_cache", true,
            "return_full_text", false,
            
            // Performance settings
            "timeout", timeoutMs,
            "retry_attempts", retryAttempts
        );
    }

    /**
     * Prompt templates for different AI use cases.
     * 
     * Templates provide consistent formatting for AI requests:
     * - Assessment analysis prompt template
     * - Career recommendation prompt template
     * - Skills gap analysis prompt template
     * - Learning suggestion prompt template
     * 
     * Template Variables:
     * - {user_profile}: User profile information
     * - {assessment_responses}: Assessment answers
     * - {career_goals}: User's stated career objectives
     * - {skills}: Current skills and experience
     * 
     * @return Map of prompt templates
     */
    @Bean("aiPromptTemplates")
    public Map<String, String> aiPromptTemplates() {
        return Map.of(
            "assessment-analysis", 
            "Analyze the following assessment responses for a woman in STEM: {assessment_responses}. " +
            "User profile: {user_profile}. Provide personalized insights on strengths, areas for " +
            "improvement, and career recommendations. Focus on empowerment and practical advice.",
            
            "career-recommendations",
            "Based on the user's profile {user_profile} and career goals {career_goals}, suggest " +
            "specific career paths in STEM. Consider work-life balance, current skills {skills}, " +
            "and growth opportunities for women in technology.",
            
            "skills-gap-analysis",
            "Compare the user's current skills {skills} with their target role requirements. " +
            "Identify skill gaps and recommend specific learning resources, courses, or experiences " +
            "to bridge these gaps. Focus on actionable steps.",
            
            "learning-suggestions",
            "Based on the assessment results {assessment_responses} and career goals {career_goals}, " +
            "suggest personalized learning paths. Include online courses, certifications, projects, " +
            "and networking opportunities relevant to women in STEM."
        );
    }
}
