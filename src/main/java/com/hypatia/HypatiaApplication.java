package com.hypatia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for hypatIA Spring Boot backend.
 * 
 * This is the entry point for the hypatIA Skills Management Platform backend.
 * The application provides REST APIs for:
 * - User authentication with JWT and OTP verification
 * - User profile management and assessment system
 * - AI integration with Hugging Face for personalized insights
 * - PDF report generation for assessment results
 * - Response caching for improved performance
 * 
 * Key Features Enabled:
 * - @SpringBootApplication: Auto-configuration and component scanning
 * - @EnableCaching: Caffeine cache for AI responses and database queries
 * - @EnableAsync: Asynchronous processing for email and AI operations
 * - @EnableTransactionManagement: Database transaction management
 * 
 * Architecture:
 * - Spring Boot MVC pattern with clear layer separation
 * - JPA/Hibernate for database operations
 * - Spring Security for authentication and authorization
 * - Spring WebClient for external API integration
 * - iText for PDF generation
 * 
 * Development Configuration:
 * - H2 in-memory database for development
 * - Auto-reload with Spring DevTools
 * - Comprehensive logging and monitoring
 * 
 * Production Configuration:
 * - PostgreSQL database with connection pooling
 * - Environment-specific configurations
 * - Health checks and metrics via Actuator
 * 
 * To run the application:
 * mvn spring-boot:run
 * 
 * The server will start on port 8000 (configurable in application.yml)
 * 
 * @author hypatIA Development Team
 * @version 1.0.0
 * @since 2025-01-22
 */
@SpringBootApplication
@EnableCaching  // Enable caching for AI responses and database queries
@EnableAsync    // Enable asynchronous processing for email and AI operations
@EnableTransactionManagement // Enable declarative transaction management
public class HypatiaApplication {

    /**
     * Main method to start the Spring Boot application.
     * 
     * This method initializes the Spring application context and starts
     * the embedded Tomcat server. The application will:
     * 
     * 1. Load configuration from application.yml files
     * 2. Initialize database connections (H2 or PostgreSQL)
     * 3. Set up security configurations and JWT handling
     * 4. Configure caching and async processing
     * 5. Start the web server on the configured port
     * 
     * Environment Variables:
     * - SPRING_PROFILES_ACTIVE: Set to 'dev', 'prod', or 'test'
     * - DATABASE_URL: PostgreSQL connection string for production
     * - HUGGINGFACE_API_KEY: API key for AI integration
     * - EMAIL_HOST, EMAIL_USERNAME, EMAIL_PASSWORD: Email configuration
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(HypatiaApplication.class, args);
        
        // Log application startup information
        System.out.println("🚀 hypatIA Spring Boot Backend Started Successfully!");
        System.out.println("📋 Health check: http://localhost:8000/health");
        System.out.println("🔗 API Base: http://localhost:8000/api");
        System.out.println("🗄️ H2 Console (dev): http://localhost:8000/h2-console");
        System.out.println("📊 Actuator: http://localhost:8000/actuator");
        System.out.println("💡 Profile: " + System.getProperty("spring.profiles.active", "default"));
    }
}
