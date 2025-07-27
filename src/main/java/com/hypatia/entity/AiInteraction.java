package com.hypatia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AiInteraction entity for storing AI service interactions and responses.
 * Optimized to log raw request/response payloads while supporting Spring-side caching.
 *
 * @author hypatIA Development Team
 */
@Entity
@Table(name = "ai_interactions", indexes = {
        @Index(name = "idx_ai_user", columnList = "user_id"),
        @Index(name = "idx_ai_type", columnList = "interaction_type"),
        @Index(name = "idx_ai_created", columnList = "created_at"),
        @Index(name = "idx_ai_cache_key", columnList = "cache_key") // Restore this index
})
public class AiInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_interaction_id")
    private Long aiInteractionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required for AI interaction")
    private User user;

    @Column(name = "interaction_type", nullable = false, length = 50)
    @NotBlank(message = "Interaction type is required")
    @Size(max = 50, message = "Interaction type must not exceed 50 characters")
    private String interactionType;


    @Column(name = "request_payload", nullable = false)
    @NotBlank(message = "Request payload to AI service is required")
    @Size(max = 10000, message = "Request payload must not exceed 10000 characters")
    private String requestPayload;


    @Column(name = "response_payload", nullable = false)
    @NotBlank(message = "Response payload from AI service is required")
    @Size(max = 20000, message = "Response payload must not exceed 20000 characters")
    private String responsePayload;

    /**
     * Cache key for response caching and deduplication.
     * Restored to support Spring-side caching.
     */
    @Column(name = "cache_key", length = 128)
    @Size(max = 128, message = "Cache key must not exceed 128 characters")
    private String cacheKey;

    /**
     * Flag indicating if this response was served from cache.
     * Restored to support Spring-side caching.
     */
    @Column(name = "is_cached_response", nullable = false)
    @NotNull(message = "isCachedResponse is required")
    private Boolean isCachedResponse = false; // Default to false

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AiInteraction() {}

    /**
     * Constructor for creating new AI interactions with basic request/response logging
     * and specific caching information.
     *
     * @param user User initiating the interaction.
     * @param interactionType Type of AI interaction (as known by Spring).
     * @param requestPayload The full JSON request sent to FastAPI.
     * @param responsePayload The full JSON response received from FastAPI.
     * @param cacheKey The unique key used for caching this interaction.
     * @param isCachedResponse True if this response was served from cache, false otherwise.
     */
    public AiInteraction(User user, String interactionType, String requestPayload, String responsePayload, String cacheKey, Boolean isCachedResponse) {
        this.user = user;
        this.interactionType = interactionType;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.cacheKey = cacheKey;
        this.isCachedResponse = isCachedResponse;
    }

    // --- Getters and Setters ---
    public Long getId() { return aiInteractionId; }
    public void setId(Long id) { this.aiInteractionId = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }
    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }
    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
    public Boolean getIsCachedResponse() { return isCachedResponse; }
    public void setIsCachedResponse(Boolean isCachedResponse) { this.isCachedResponse = isCachedResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "AiInteraction{" +
                "ai_interaction_id=" + aiInteractionId +
                ", userId=" + (user != null ? user.getId() : null) +
                ", interactionType='" + interactionType + '\'' +
                ", cacheKey='" + cacheKey + '\'' +
                ", isCached=" + isCachedResponse +
                ", createdAt=" + createdAt +
                '}';
    }
}