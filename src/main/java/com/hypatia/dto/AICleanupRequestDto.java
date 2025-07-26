package com.hypatia.dto;

import jakarta.validation.constraints.Min; // Import for validation annotation
import jakarta.validation.constraints.NotNull; // Import for validation annotation

/**
 * Data Transfer Object for requesting cleanup of old AI interactions.
 * Specifies the age (in days) of interactions to be removed.
 */
public class AICleanupRequestDto {

    /**
     * Number of days older than which AI interactions should be cleaned up.
     * Must be a positive integer. Default to 30 days.
     */
    @NotNull(message = "Older than days is required for cleanup.")
    @Min(value = 1, message = "Older than days must be at least 1.")
    private Integer olderThanDays = 30; // Default value

    // Default constructor for JSON deserialization
    public AICleanupRequestDto() {}

    // Constructor for convenience (optional)
    public AICleanupRequestDto(Integer olderThanDays) {
        this.olderThanDays = olderThanDays;
    }

    // Getter and Setter
    public Integer getOlderThanDays() {
        return olderThanDays;
    }

    public void setOlderThanDays(Integer olderThanDays) {
        this.olderThanDays = olderThanDays;
    }

    @Override
    public String toString() {
        return "AICleanupRequestDto{" +
                "olderThanDays=" + olderThanDays +
                '}';
    }
}