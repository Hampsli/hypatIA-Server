package com.hypatia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * DTO for sending a request to the generic AI analysis endpoint.
 * Primarily carries the direct 'text' to be sent to FastAPI.
 */
public class AIRequestDto {
    @NotBlank(message = "Analysis type is required.")
    // Examples: "ASSESSMENT_ANALYSIS", "CAREER_RECOMMENDATIONS", "LEARNING_PATH", "SKILLS_GAP", "INDUSTRY_INSIGHTS", "CHAT"
    private String analysisType;

    @NotBlank(message = "The text input for AI analysis is required.")
    @Size(max = 5000, message = "Text input cannot exceed 5000 characters.") // Add a reasonable size limit
    private String text; // The direct text from the frontend for FastAPI

    // Optional: Keep for logging or if FastAPI occasionally uses additional context
    private Map<String, Object> additionalContext; // Renamed from contextData for clarity

    // Getters and Setters
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Map<String, Object> getAdditionalContext() { return additionalContext; }
    public void setAdditionalContext(Map<String, Object> additionalContext) { this.additionalContext = additionalContext; }
}