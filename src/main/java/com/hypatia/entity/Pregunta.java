package com.hypatia.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa una pregunta dentro de un cuestionario.
 * Cada pregunta está asociada a un Cuestionario y a una Sección específicos.
 */
@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    private CuestionarioSeccion seccion;

    @Column(name = "texto_pregunta", nullable = false, columnDefinition = "TEXT")
    private String textoPregunta;

    @Column(name = "texto_ayuda", columnDefinition = "TEXT")
    private String textoAyuda;

    @Column(name = "tipo_pregunta", nullable = false, length = 50)
    private String tipoPregunta;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<OpcionPregunta> opciones;

    // Constructores, Getters y Setters
    public Pregunta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cuestionario getCuestionario() { return cuestionario; }
    public void setCuestionario(Cuestionario cuestionario) { this.cuestionario = cuestionario; }
    public CuestionarioSeccion getSeccion() { return seccion; }
    public void setSeccion(CuestionarioSeccion seccion) { this.seccion = seccion; }
    public String getTextoPregunta() { return textoPregunta; }
    public void setTextoPregunta(String textoPregunta) { this.textoPregunta = textoPregunta; }
    public String getTextoAyuda() { return textoAyuda; }
    public void setTextoAyuda(String textoAyuda) { this.textoAyuda = textoAyuda; }
    public String getTipoPregunta() { return tipoPregunta; }
    public void setTipoPregunta(String tipoPregunta) { this.tipoPregunta = tipoPregunta; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public List<OpcionPregunta> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionPregunta> opciones) { this.opciones = opciones; }
}