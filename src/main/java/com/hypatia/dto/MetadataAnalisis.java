package com.hypatia.dto;

import java.util.ArrayList;

public class MetadataAnalisis {

    private Boolean revision_humana_necesaria;
    private ArrayList<String> razones_complejidad;
    private Integer puntuacion_complejidad;

    public Boolean getRevision_humana_necesaria() {
        return revision_humana_necesaria;
    }

    public void setRevision_humana_necesaria(Boolean revision_humana_necesaria) {
        this.revision_humana_necesaria = revision_humana_necesaria;
    }

    public ArrayList<String> getRazones_complejidad() {
        return razones_complejidad;
    }

    public void setRazones_complejidad(ArrayList<String> razones_complejidad) {
        this.razones_complejidad = razones_complejidad;
    }

    public Integer getPuntuacion_complejidad() {
        return puntuacion_complejidad;
    }

    public void setPuntuacion_complejidad(Integer puntuacion_complejidad) {
        this.puntuacion_complejidad = puntuacion_complejidad;
    }
}
