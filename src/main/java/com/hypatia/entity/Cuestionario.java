package com.hypatia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa la definición de un cuestionario o escenario.
 * Es el objeto de más alto nivel en la jerarquía del módulo.
 */
@Entity
@Table(name = "cuestionarios",schema = "public")
public class Cuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fase_cuestionario", nullable = false, length = 50)
    private String faseCuestionario;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<CuestionarioSeccion> secciones;

    // Constructores, Getters y Setters
    public Cuestionario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFaseCuestionario() { return faseCuestionario; }
    public void setFaseCuestionario(String faseCuestionario) { this.faseCuestionario = faseCuestionario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public List<CuestionarioSeccion> getSecciones() { return secciones; }
    public void setSecciones(List<CuestionarioSeccion> secciones) { this.secciones = secciones; }
}