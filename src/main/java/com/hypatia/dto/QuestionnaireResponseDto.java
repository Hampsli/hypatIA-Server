package com.hypatia.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Data Transfer Object para enviar la respuesta a una pregunta del cuestionario.
 *
 * Este DTO encapsula la información de una única respuesta de un usuario,
 * incluyendo el ID de la pregunta y el valor de la respuesta proporcionada.
 * Una lista de estos objetos es enviada por el cliente al endpoint de envío.
 *
 * Reglas de Validación:
 * - questionId: Requerido, ya que cada respuesta debe estar asociada a una pregunta.
 * - answer: El contenido puede variar (texto, valor de una opción, etc.).
 *
 * Usado por:
 * - El endpoint POST /api/questionnaires/responses.
 * - El servicio QuestionnaireService para procesar y guardar las respuestas.
 */
public class QuestionnaireResponseDto {

    /**
     * El identificador único de la pregunta que se está respondiendo.
     * Este campo es obligatorio.
     */
    @NotNull(message = "Question ID is required for every response.")
    private Long questionId;

    /**
     * La respuesta proporcionada por el usuario.
     * Para preguntas de selección, este sería el 'valor_opcion' (ej. "femenino").
     * Para preguntas de texto, sería el texto introducido por el usuario.
     */
    private String answer;

    /**
     * Default constructor para la deserialización de JSON.
     */
    public QuestionnaireResponseDto() {}

    /**
     * Constructor completo para crear un DTO de respuesta.
     *
     * @param questionId El ID de la pregunta.
     * @param answer La respuesta del usuario.
     */
    public QuestionnaireResponseDto(Long questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }


    @Override
    public String toString() {
        return "QuestionnaireResponseDto{" +
                "questionId=" + questionId +
                ", answer='" + answer + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireResponseDto that = (QuestionnaireResponseDto) o;
        return Objects.equals(questionId, that.questionId) && Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, answer);
    }
}