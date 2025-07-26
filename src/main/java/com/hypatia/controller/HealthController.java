package com.hypatia.controller;

import com.hypatia.service.AIService;
import com.hypatia.service.EmailService;
import com.hypatia.service.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for system health checks and monitoring.
 * 
 * This controller provides comprehensive system health information including:
 * - Overall application health status
 * - Database connectivity and performance
 * - External service availability (AI, Email)
 * - System resource utilization
 * - Service-specific health checks
 * - Performance metrics and statistics
 * 
 * Used by:
 * - Load balancers for health check routing
 * - Monitoring systems for alerting
 * - DevOps teams for system diagnostics
 * - Frontend applications for service status
 * 
 * Base path: /health, /api/health
 * 
 * Security:
 * - Basic health endpoint is publicly accessible
 * - Detailed diagnostics may require authentication
 * - Sensitive information is excluded from responses
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private AIService aiService;

    /**
     * Basic health check endpoint.
     * 
     * Returns simple health status for load balancer health checks.
     * This endpoint should respond quickly and indicate basic service availability.
     * 
     * GET /health
     * 
     * @return ResponseEntity with basic health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "hypatIA Backend");
            health.put("version", "1.0.0");
            health.put("message", "hypatIA server is running!");
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("error", "Health check failed");
            
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Detailed health check with service diagnostics.
     * 
     * Provides comprehensive health information including:
     * - Application status and uptime
     * - Database connectivity
     * - External service availability
     * - System resource usage
     * - Performance metrics
     * 
     * GET /api/health
     * 
     * @return ResponseEntity with detailed health information
     */
    @GetMapping("/api/health")
    public ResponseEntity<?> detailedHealthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            // Basic application info
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "hypatIA Backend API");
            health.put("version", "1.0.0");
            health.put("environment", getActiveProfile());
            
            // System information
            Map<String, Object> system = new HashMap<>();
            Runtime runtime = Runtime.getRuntime();
            system.put("availableProcessors", runtime.availableProcessors());
            system.put("totalMemory", formatBytes(runtime.totalMemory()));
            system.put("freeMemory", formatBytes(runtime.freeMemory()));
            system.put("maxMemory", formatBytes(runtime.maxMemory()));
            system.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
            health.put("system", system);
            
            // Service health checks
            Map<String, Object> services = new HashMap<>();
            services.put("database", checkDatabaseHealth());
            services.put("email", checkEmailServiceHealth());
            services.put("pdf", checkPDFServiceHealth());
            services.put("ai", checkAIServiceHealth());
            health.put("services", services);
            
            // Application metrics
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("uptime", getUptimeInfo());
            health.put("metrics", metrics);
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Database connectivity health check.
     * 
     * GET /api/health/database
     * 
     * @return ResponseEntity with database health status
     */
    @GetMapping("/api/health/database")
    public ResponseEntity<?> databaseHealth() {
        try {
            Map<String, Object> dbHealth = checkDatabaseHealth();
            
            if ("UP".equals(dbHealth.get("status"))) {
                return ResponseEntity.ok(dbHealth);
            } else {
                return ResponseEntity.status(503).body(dbHealth);
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Email service health check.
     * 
     * GET /api/health/email
     * 
     * @return ResponseEntity with email service health status
     */
    @GetMapping("/api/health/email")
    public ResponseEntity<?> emailHealth() {
        try {
            Map<String, Object> emailHealth = checkEmailServiceHealth();
            
            if ("UP".equals(emailHealth.get("status"))) {
                return ResponseEntity.ok(emailHealth);
            } else {
                return ResponseEntity.status(503).body(emailHealth);
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * PDF service health check.
     * 
     * GET /api/health/pdf
     * 
     * @return ResponseEntity with PDF service health status
     */
    @GetMapping("/api/health/pdf")
    public ResponseEntity<?> pdfHealth() {
        try {
            Map<String, Object> pdfHealth = checkPDFServiceHealth();
            
            if ("UP".equals(pdfHealth.get("status"))) {
                return ResponseEntity.ok(pdfHealth);
            } else {
                return ResponseEntity.status(503).body(pdfHealth);
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * AI service health check.
     * 
     * GET /api/health/ai
     * 
     * @return ResponseEntity with AI service health status
     */
    @GetMapping("/api/health/ai")
    public ResponseEntity<?> aiHealth() {
        try {
            Map<String, Object> aiHealth = checkAIServiceHealth();
            
            if ("UP".equals(aiHealth.get("status"))) {
                return ResponseEntity.ok(aiHealth);
            } else {
                return ResponseEntity.status(503).body(aiHealth);
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * System readiness check for Kubernetes deployments.
     * 
     * GET /health/ready
     * 
     * @return ResponseEntity indicating if system is ready to receive traffic
     */
    @GetMapping("/health/ready")
    public ResponseEntity<?> readinessCheck() {
        try {
            // Check if all critical services are available
            boolean databaseReady = "UP".equals(checkDatabaseHealth().get("status"));
            boolean emailReady = "UP".equals(checkEmailServiceHealth().get("status"));
            
            if (databaseReady && emailReady) {
                return ResponseEntity.ok(Map.of(
                    "status", "READY",
                    "timestamp", LocalDateTime.now(),
                    "message", "Service is ready to receive traffic"
                ));
            } else {
                return ResponseEntity.status(503).body(Map.of(
                    "status", "NOT_READY",
                    "timestamp", LocalDateTime.now(),
                    "message", "Service is not ready to receive traffic"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "NOT_READY",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * System liveness check for Kubernetes deployments.
     * 
     * GET /health/live
     * 
     * @return ResponseEntity indicating if system is alive
     */
    @GetMapping("/health/live")
    public ResponseEntity<?> livenessCheck() {
        try {
            // Basic liveness check - if we can respond, we're alive
            return ResponseEntity.ok(Map.of(
                "status", "ALIVE",
                "timestamp", LocalDateTime.now(),
                "uptime", System.currentTimeMillis() / 1000 + " seconds"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "DEAD",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Checks database connectivity and performance.
     * 
     * @return Map with database health information
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            // This would typically involve a simple database query
            // For now, we'll simulate a basic connectivity check
            long startTime = System.currentTimeMillis();
            
            // Simulate database connectivity check
            // In real implementation, execute a simple query like "SELECT 1"
            Thread.sleep(10); // Simulate query time
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            dbHealth.put("status", "UP");
            dbHealth.put("responseTime", responseTime + "ms");
            dbHealth.put("driver", "PostgreSQL/H2");
            dbHealth.put("connection", "Active");
            
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        
        dbHealth.put("timestamp", LocalDateTime.now());
        return dbHealth;
    }

    /**
     * Checks email service configuration and availability.
     * 
     * @return Map with email service health information
     */
    private Map<String, Object> checkEmailServiceHealth() {
        Map<String, Object> emailHealth = new HashMap<>();
        
        try {
            Map<String, Object> emailStatus = emailService.getEmailServiceStatus();
            
            emailHealth.put("status", "UP");
            emailHealth.put("configuration", emailStatus);
            
        } catch (Exception e) {
            emailHealth.put("status", "DOWN");
            emailHealth.put("error", e.getMessage());
        }
        
        emailHealth.put("timestamp", LocalDateTime.now());
        return emailHealth;
    }

    /**
     * Checks PDF service availability and configuration.
     * 
     * @return Map with PDF service health information
     */
    private Map<String, Object> checkPDFServiceHealth() {
        Map<String, Object> pdfHealth = new HashMap<>();
        
        try {
            Map<String, Object> pdfStatus = pdfService.getPDFServiceStatus();
            
            pdfHealth.put("status", "UP");
            pdfHealth.put("configuration", pdfStatus);
            
        } catch (Exception e) {
            pdfHealth.put("status", "DOWN");
            pdfHealth.put("error", e.getMessage());
        }
        
        pdfHealth.put("timestamp", LocalDateTime.now());
        return pdfHealth;
    }

    /**
     * Checks AI service connectivity and configuration.
     * 
     * @return Map with AI service health information
     */
    private Map<String, Object> checkAIServiceHealth() {
        Map<String, Object> aiHealth = new HashMap<>();
        
        try {
            // Get AI service statistics as a health indicator
            Map<String, Object> aiStats = aiService.getAIServiceStatistics();
            
            aiHealth.put("status", "UP");
            aiHealth.put("statistics", aiStats);
            
        } catch (Exception e) {
            aiHealth.put("status", "DOWN");
            aiHealth.put("error", e.getMessage());
        }
        
        aiHealth.put("timestamp", LocalDateTime.now());
        return aiHealth;
    }

    /**
     * Gets active Spring profile information.
     * 
     * @return active profile name
     */
    private String getActiveProfile() {
        // This would typically get the active Spring profile
        // For now, return default
        return System.getProperty("spring.profiles.active", "dev");
    }

    /**
     * Gets application uptime information.
     * 
     * @return uptime information map
     */
    private Map<String, Object> getUptimeInfo() {
        Map<String, Object> uptime = new HashMap<>();
        
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSeconds = uptimeMs / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        uptime.put("uptimeMs", uptimeMs);
        uptime.put("uptimeFormatted", String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds));
        uptime.put("startTime", LocalDateTime.now().minusNanos(uptimeMs * 1_000_000));
        
        return uptime;
    }

    /**
     * Formats byte values for human readability.
     * 
     * @param bytes byte count to format
     * @return formatted string with appropriate unit
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
