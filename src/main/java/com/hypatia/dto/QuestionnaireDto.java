package com.hypatia.dto;

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for a full questionnaire.
 *
 * This is the top-level DTO that represents an entire questionnaire,
 * including its metadata and a nested structure of all its sections,
 * questions, and options. It is designed to be returned by the API
 * to a client application for rendering.
 *
 * Used by:
 * - Questionnaire API endpoint (e.g., GET /api/questionnaires/{id}).
 * - Frontend applications to dynamically build and display the questionnaire.
 */
public class QuestionnaireDto {

    /**
     * Unique identifier for the questionnaire.
     */
    private Long id;

    /**
     * The name or title of the questionnaire.
     * Example: "Diagnóstico de Onboarding".
     */
    private String name;

    /**
     * A detailed description of the questionnaire's purpose.
     */
    private String description;

    /**
     * The business phase this questionnaire belongs to.
     * Example: "onboarding".
     */
    private String phase;

    /**
     * The list of all sections contained within this questionnaire, ordered.
     */
    private List<SectionDto> sections;

    /**
     * Default constructor for JSON deserialization.
     */
    public QuestionnaireDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public List<SectionDto> getSections() { return sections; }
    public void setSections(List<SectionDto> sections) { this.sections = sections; }

    @Override
    public String toString() {
        return "QuestionnaireDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phase='" + phase + '\'' +
                ", sectionsCount=" + (sections != null ? sections.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireDto that = (QuestionnaireDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}