package com.hypatia.dto;

import java.util.Map;

/**
 * DTO for receiving a generic AI response.
 * The actual structure depends on the FastAPI's /api/analizar output.
 */
public class AIResponseDto {
    private Map<String, Object> responseContent; // Or String if FastAPI returns plain text JSON

    // Default constructor
    public AIResponseDto() {}

    // Constructor for convenience
    public AIResponseDto(Map<String, Object> responseContent) {
        this.responseContent = responseContent;
    }

    // Getters and Setters
    public Map<String, Object> getResponseContent() { return responseContent; }
    public void setResponseContent(Map<String, Object> responseContent) { this.responseContent = responseContent; }
}