package com.hypatia.config;

import com.hypatia.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for hypatIA application.
 * 
 * This configuration class sets up:
 * - JWT-based authentication with custom filter
 * - CORS configuration for frontend integration
 * - Password encoding with BCrypt
 * - Stateless session management
 * - Endpoint security rules
 * 
 * Security Architecture:
 * - JWT tokens for authentication (no sessions)
 * - BCrypt password hashing with strength 12
 * - CORS enabled for frontend communication
 * - Public endpoints for registration and health checks
 * - Protected endpoints requiring valid JWT tokens
 * 
 * Authentication Flow:
 * 1. User registers/logs in with credentials
 * 2. OTP verification required for security
 * 3. JWT token issued upon successful verification
 * 4. Token included in Authorization header for API calls
 * 5. JwtAuthenticationFilter validates token on each request
 * 
 * Required Libraries:
 * - spring-boot-starter-security
 * - jjwt-api, jjwt-impl, jjwt-jackson for JWT handling
 * 
 * @author hypatIA Development Team
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder bean using BCrypt algorithm.
     * 
     * BCrypt is a secure hashing algorithm that includes:
     * - Salt generation for each password
     * - Configurable strength (12 rounds for good security/performance balance)
     * - Resistance to rainbow table attacks
     * 
     * Usage:
     * - passwordEncoder.encode(rawPassword) to hash passwords
     * - passwordEncoder.matches(rawPassword, encodedPassword) to verify
     * 
     * @return BCryptPasswordEncoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication manager bean for authentication operations.
     * 
     * This manager handles:
     * - User credential validation
     * - Integration with UserDetailsService
     * - Authentication provider configuration
     * 
     * @param authConfig Authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main security filter chain configuration.
     * 
     * This method configures:
     * - CORS settings for frontend integration
     * - CSRF protection (disabled for API-only backend)
     * - Session management (stateless for JWT)
     * - Request authorization rules
     * - JWT filter integration
     * 
     * Public Endpoints (no authentication required):
     * - /api/auth/** - Registration, login, OTP verification
     * - /health - Health check endpoint
     * - /actuator/health - Spring Boot health endpoint
     * - /h2-console/** - H2 database console (development only)
     * 
     * Protected Endpoints (JWT token required):
     * - /api/profile - User profile management
     * - /api/assessment/** - Assessment system
     * - /api/ai/** - AI integration endpoints
     * 
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain with configured security rules
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for API-only backend
            .csrf(csrf -> csrf.disable())
            
            // Configure session management as stateless (no sessions with JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure request authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Development only
                
                // Protected endpoints - JWT authentication required
                .requestMatchers("/api/profile/**").authenticated()
                .requestMatchers("/api/assessment/**").authenticated()
                .requestMatchers("/api/ai/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter before default authentication filter
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class)
            
            // Disable frame options for H2 console (development only)
                .headers(headers ->
                        headers.contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'"))
                );

        return http.build();
    }

    /**
     * CORS configuration for frontend integration.
     * 
     * Configures Cross-Origin Resource Sharing to allow:
     * - Frontend applications on localhost:3000 and localhost:5000
     * - All HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
     * - Common headers including Authorization for JWT tokens
     * - Credentials for authenticated requests
     * 
     * Production Configuration:
     * - Update allowed origins to include production frontend URLs
     * - Consider more restrictive CORS policies for production
     * - Monitor CORS-related security headers
     * 
     * @return CorsConfigurationSource with configured CORS rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (frontend applications)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",  // React development server
            "http://localhost:5000",  // Alternative frontend port
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5000"
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow common headers including Authorization for JWT
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Cache-Control"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
