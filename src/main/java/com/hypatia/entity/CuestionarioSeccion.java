package com.hypatia.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa una sección dentro de un cuestionario.
 * Agrupa un conjunto de preguntas relacionadas.
 */
@Entity
@Table(name = "cuestionario_secciones")
public class CuestionarioSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @Column(name = "nombre_seccion", nullable = false, length = 255)
    private String nombreSeccion;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Pregunta> preguntas;

    // Constructores, Getters y Setters
    public CuestionarioSeccion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cuestionario getCuestionario() { return cuestionario; }
    public void setCuestionario(Cuestionario cuestionario) { this.cuestionario = cuestionario; }
    public String getNombreSeccion() { return nombreSeccion; }
    public void setNombreSeccion(String nombreSeccion) { this.nombreSeccion = nombreSeccion; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }
}