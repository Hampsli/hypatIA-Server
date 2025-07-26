package com.hypatia.dto;

import java.util.Objects;

/**
 * Data Transfer Object for a question option.
 *
 * This DTO represents a single selectable answer within a multiple-choice
 * or single-choice question.
 *
 * Used by:
 * - QuestionDto to list available options.
 * - Questionnaire API endpoint to build the full questionnaire structure.
 */
public class OptionDto {

    /**
     * Unique identifier for the option.
     */
    private Long id;

    /**
     * The visible text of the option presented to the user.
     * Example: "Femenino", "Tiempo completo", "Python".
     */
    private String text;

    /**
     * The actual value stored when this option is selected.
     * Example: "femenino", "tiempo_completo", "python".
     */
    private String value;

    /**
     * The display order of this option within the question.
     */
    private Integer order;

    /**
     * Default constructor for JSON deserialization.
     */
    public OptionDto() {}

    /**
     * Full constructor for creating an option DTO.
     */
    public OptionDto(Long id, String text, String value, Integer order) {
        this.id = id;
        this.text = text;
        this.value = value;
        this.order = order;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    @Override
    public String toString() {
        return "OptionDto{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", value='" + value + '\'' +
                ", order=" + order +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionDto optionDto = (OptionDto) o;
        return Objects.equals(id, optionDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}