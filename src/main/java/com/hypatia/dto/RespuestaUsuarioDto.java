package com.hypatia.dto;

/**
 * DTO para representar una respuesta guardada de un usuario.
 * Se usa para mostrar el historial de respuestas.
 */
public class RespuestaUsuarioDto {

    private Long questionId;
    private String questionText;
    private String answer;

    // Constructores, Getters y Setters
    public RespuestaUsuarioDto() {}

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}