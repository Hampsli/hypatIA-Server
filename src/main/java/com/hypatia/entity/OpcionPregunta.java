package com.hypatia.entity;

import jakarta.persistence.*;

/**
 * Entidad que representa una opción de respuesta para una pregunta específica.
 */
@Entity
@Table(name = "opciones_pregunta",schema = "public")
public class OpcionPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "texto_opcion", nullable = false, length = 255)
    private String textoOpcion;

    @Column(name = "valor_opcion", length = 255)
    private String valorOpcion;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    // Constructores, Getters y Setters
    public OpcionPregunta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
    public String getTextoOpcion() { return textoOpcion; }
    public void setTextoOpcion(String textoOpcion) { this.textoOpcion = textoOpcion; }
    public String getValorOpcion() { return valorOpcion; }
    public void setValorOpcion(String valorOpcion) { this.valorOpcion = valorOpcion; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}