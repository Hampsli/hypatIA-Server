package com.hypatia.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que almacena la respuesta de un usuario a una pregunta del cuestionario.
 *
 * Esta tabla es el núcleo para rastrear el progreso de un usuario y analizar
 * sus respuestas a lo largo del tiempo. Vincula a un Usuario con una Pregunta específica.
 */
@Entity
@Table(name = "respuestas_usuario", indexes = {
        @Index(name = "idx_respuesta_usuario", columnList = "user_id"),
        @Index(name = "idx_respuesta_pregunta", columnList = "pregunta_id")
})
public class RespuestaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El usuario que proporcionó la respuesta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * La pregunta del cuestionario que se está respondiendo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    /**
     * El contenido de la respuesta del usuario.
     * Este campo es flexible:
     * - Para preguntas de selección, almacena el 'valor_opcion' de la OpcionPregunta elegida.
     * - Para preguntas de texto libre (texto_corto, texto_largo), almacena el texto introducido.
     * - Para preguntas de archivo, podría almacenar la URL del archivo subido.
     */
    @Column(name = "respuesta_texto", columnDefinition = "TEXT")
    private String respuestaTexto;

    /**
     * Un identificador para agrupar todas las respuestas de un mismo intento de cuestionario.
     * Útil para diferenciar entre múltiples veces que un usuario toma el mismo cuestionario.
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * Indica si esta es la respuesta más reciente del usuario para esta pregunta,
     * en caso de que se permita volver a tomar el cuestionario.
     */
    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = true;

    /**
     * La fecha y hora en que se registró la respuesta.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Constructores ---

    public RespuestaUsuario() {}

    public RespuestaUsuario(User user, Pregunta pregunta, String respuestaTexto, String sessionId) {
        this.user = user;
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
        this.sessionId = sessionId;
        this.isCurrent = true;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public String getRespuestaTexto() {
        return respuestaTexto;
    }

    public void setRespuestaTexto(String respuestaTexto) {
        this.respuestaTexto = respuestaTexto;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}