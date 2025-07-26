package com.hypatia.dto;

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for a single question.
 *
 * This DTO encapsulates all details of a question, including its type,
 * help text, and a list of possible options if applicable.
 *
 * Used by:
 * - SectionDto to build a list of questions within a section.
 */
public class QuestionDto {

    /**
     * Unique identifier for the question.
     */
    private Long id;

    /**
     * The main text of the question.
     */
    private String text;

    /**
     * Optional helper text to provide context or instructions.
     */
    private String helpText;

    /**
     * The type of question, which determines the UI component to render.
     * Examples: "email", "texto_corto", "selector_simple", "archivo".
     */
    private String type;

    /**
     * The display order of the question within its section.
     */
    private Integer order;

    /**
     * A list of possible answers for 'selector_simple' or 'selector_multiple' types.
     * This list will be empty for other question types.
     */
    private List<OptionDto> options;

    /**
     * Default constructor for JSON deserialization.
     */
    public QuestionDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public List<OptionDto> getOptions() { return options; }
    public void setOptions(List<OptionDto> options) { this.options = options; }

    @Override
    public String toString() {
        return "QuestionDto{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", type='" + type + '\'' +
                ", order=" + order +
                ", optionsCount=" + (options != null ? options.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionDto that = (QuestionDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}