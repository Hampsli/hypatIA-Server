package com.hypatia.dto;

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for a questionnaire section.
 *
 * A section is a logical grouping of questions within a questionnaire,
 * such as "Información Personal" or "Autoevaluación de Habilidades".
 *
 * Used by:
 * - QuestionnaireDto to build the complete structure of a questionnaire.
 */
public class SectionDto {

    /**
     * Unique identifier for the section.
     */
    private Long id;

    /**
     * The name or title of the section.
     */
    private String name;

    /**
     * The display order of the section within the questionnaire.
     */
    private Integer order;

    /**
     * The list of questions belonging to this section.
     */
    private List<QuestionDto> questions;

    /**
     * Default constructor for JSON deserialization.
     */
    public SectionDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }

    @Override
    public String toString() {
        return "SectionDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", order=" + order +
                ", questionsCount=" + (questions != null ? questions.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionDto that = (SectionDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}