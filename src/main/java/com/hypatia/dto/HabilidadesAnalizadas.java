package com.hypatia.dto;

public class HabilidadesAnalizadas {
    private String habilidad;
    private String competencia_ligada;
    private String nivel_desarrollo;
    private String observaciones;

    public String getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(String habilidad) {
        this.habilidad = habilidad;
    }

    public String getCompetencia_ligada() {
        return competencia_ligada;
    }

    public void setCompetencia_ligada(String competencia_ligada) {
        this.competencia_ligada = competencia_ligada;
    }

    public String getNivel_desarrollo() {
        return nivel_desarrollo;
    }

    public void setNivel_desarrollo(String nivel_desarrollo) {
        this.nivel_desarrollo = nivel_desarrollo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
