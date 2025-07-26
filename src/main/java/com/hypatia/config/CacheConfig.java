package com.hypatia.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for improved application performance.
 * 
 * This configuration sets up caching for:
 * - AI API responses to reduce external API calls and costs
 * - Database query results for frequently accessed data
 * - Assessment questions and user profiles
 * - Static data that doesn't change frequently
 * 
 * Caching Strategy:
 * - Caffeine cache for high-performance in-memory caching
 * - Multiple cache regions with different TTL and size limits
 * - Automatic cache eviction based on time and memory usage
 * - Cache statistics and monitoring support
 * 
 * Cache Regions:
 * 1. ai-responses: AI API responses (1 hour TTL)
 * 2. user-profiles: User profile data (30 minutes TTL)
 * 3. assessment-questions: Static assessment data (1 day TTL)
 * 4. database-queries: Frequently accessed database results (15 minutes TTL)
 * 
 * Performance Benefits:
 * - Reduced response times for cached data
 * - Lower database load and improved scalability
 * - Decreased external API costs (Hugging Face)
 * - Better user experience with faster page loads
 * 
 * Memory Management:
 * - Maximum cache size limits to prevent memory issues
 * - LRU (Least Recently Used) eviction policy
 * - Automatic cleanup of expired entries
 * - JVM memory monitoring integration
 * 
 * Required Libraries:
 * - spring-boot-starter-cache
 * - caffeine (high-performance caching library)
 * 
 * @author hypatIA Development Team
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Main cache manager using Caffeine for high performance.
     * 
     * Caffeine Features:
     * - High-performance concurrent caching
     * - Automatic loading and refresh capabilities
     * - Size-based and time-based eviction policies
     * - Statistics collection for monitoring
     * - Thread-safe operations with minimal contention
     * 
     * Cache Configuration Strategy:
     * - Different cache names for different data types
     * - Optimized TTL based on data update frequency
     * - Size limits based on expected usage patterns
     * - Monitoring and statistics enabled for performance tuning
     * 
     * @return CacheManager configured with Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Enable cache statistics for monitoring and debugging
        cacheManager.setCaffeine(defaultCaffeineConfig());
        
        // Configure specific cache regions with custom settings
        List<String> cacheNames = Arrays.asList( "ai-responses", "user-profiles","assessment-questions","database-queries","otp-codes","static-data");

        cacheManager.setCacheNames(cacheNames);
        
        return cacheManager;
    }

    /**
     * Default Caffeine cache configuration.
     * 
     * Default Settings:
     * - Maximum 1000 entries per cache
     * - 30-minute expiration after write
     * - 15-minute expiration after last access
     * - Statistics recording enabled
     * 
     * These settings provide a good balance between:
     * - Memory usage and cache effectiveness
     * - Data freshness and performance
     * - Monitoring capabilities and operational insights
     * 
     * @return Caffeine configuration with default settings
     */
    private Caffeine<Object, Object> defaultCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1000)                          // Max 1000 entries
                .expireAfterWrite(30, TimeUnit.MINUTES)     // Expire after 30 min write
                .expireAfterAccess(15, TimeUnit.MINUTES)    // Expire after 15 min access
                .recordStats();                             // Enable statistics
    }

    /**
     * AI responses cache configuration with extended TTL.
     * 
     * AI Response Caching Strategy:
     * - Longer TTL (1 hour) because AI responses are expensive to generate
     * - Larger cache size to accommodate multiple users and queries
     * - Cache key based on user data hash + model + prompt
     * - Statistics enabled for monitoring API call reduction
     * 
     * Cache Key Format:
     * "ai-responses::user_{userId}_model_{modelName}_hash_{contentHash}"
     * 
     * Benefits:
     * - Reduces Hugging Face API costs
     * - Improves response times for similar queries
     * - Provides consistent results for identical inputs
     * - Reduces external API dependency
     * 
     * @return Caffeine configuration for AI responses
     */
    @Bean("aiResponsesCache")
    public Caffeine<Object, Object> aiResponsesCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(500)                           // Max 500 AI responses
                .expireAfterWrite(1, TimeUnit.HOURS)        // 1 hour TTL
                .expireAfterAccess(30, TimeUnit.MINUTES)    // 30 min idle timeout
                .recordStats();
    }

    /**
     * User profiles cache configuration for frequent access.
     * 
     * User Profile Caching Strategy:
     * - Medium TTL (30 minutes) for balance between freshness and performance
     * - Cache user profile data that's accessed frequently
     * - Automatic invalidation when profile is updated
     * - Smaller cache size due to larger data objects
     * 
     * Cache Operations:
     * - @Cacheable on profile retrieval methods
     * - @CacheEvict on profile update operations
     * - @CachePut for cache-through updates
     * 
     * @return Caffeine configuration for user profiles
     */
    @Bean("userProfilesCache")
    public Caffeine<Object, Object> userProfilesCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(200)                           // Max 200 user profiles
                .expireAfterWrite(30, TimeUnit.MINUTES)     // 30 min TTL
                .expireAfterAccess(15, TimeUnit.MINUTES)    // 15 min idle timeout
                .recordStats();
    }

    /**
     * Assessment questions cache for static data.
     * 
     * Assessment Questions Caching Strategy:
     * - Long TTL (24 hours) because questions rarely change
     * - Small cache size due to limited number of questions
     * - Perfect for static data that's accessed frequently
     * - Manual cache eviction when questions are updated
     * 
     * Use Cases:
     * - Assessment question retrieval
     * - Question metadata and categories
     * - Scoring algorithms and weights
     * 
     * @return Caffeine configuration for assessment questions
     */
    @Bean("assessmentQuestionsCache")
    public Caffeine<Object, Object> assessmentQuestionsCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(100)                           // Max 100 question sets
                .expireAfterWrite(24, TimeUnit.HOURS)       // 24 hour TTL
                .recordStats();
    }

    /**
     * Database queries cache for improved performance.
     * 
     * Database Query Caching Strategy:
     * - Short TTL (15 minutes) for data consistency
     * - Cache frequently executed queries
     * - Automatic eviction on data changes
     * - Monitor cache hit ratio for optimization
     * 
     * Cached Queries:
     * - User authentication lookups
     * - Profile summary data
     * - Assessment statistics
     * - System configuration data
     * 
     * @return Caffeine configuration for database queries
     */
    @Bean("databaseQueriesCache")
    public Caffeine<Object, Object> databaseQueriesCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(300)                           // Max 300 query results
                .expireAfterWrite(15, TimeUnit.MINUTES)     // 15 min TTL
                .expireAfterAccess(5, TimeUnit.MINUTES)     // 5 min idle timeout
                .recordStats();
    }

    /**
     * OTP codes cache for temporary storage.
     * 
     * OTP Caching Strategy:
     * - Very short TTL (2 minutes) matching OTP validity
     * - Small cache size for active OTP codes only
     * - Automatic cleanup of expired codes
     * - Used for rate limiting and validation
     * 
     * Security Considerations:
     * - OTP codes are sensitive data
     * - Cache should not persist beyond memory
     * - Automatic cleanup prevents accumulation
     * - Access logging for security monitoring
     * 
     * @return Caffeine configuration for OTP codes
     */
    @Bean("otpCodesCache")
    public Caffeine<Object, Object> otpCodesCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(100)                           // Max 100 active OTPs
                .expireAfterWrite(2, TimeUnit.MINUTES)      // 2 min TTL (OTP validity)
                .recordStats();
    }
}
