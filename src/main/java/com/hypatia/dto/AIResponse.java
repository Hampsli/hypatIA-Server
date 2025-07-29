package com.hypatia.dto;

import java.util.ArrayList;

public class AIResponse {

    private String retroalimentacion_original;
    private ArrayList<HabilidadesAnalizadas> habilidades_analizadas;
    private ArrayList<MetodologiasSugeridas> metodologias_sugeridas;
    private MetadataAnalisis metadata_analisis;

    public AIResponse() {
        habilidades_analizadas=new ArrayList<>();
        metodologias_sugeridas=new ArrayList<>();
        metadata_analisis=new MetadataAnalisis();
    }

    public String getRetroalimentacion_original() {
        return retroalimentacion_original;
    }

    public void setRetroalimentacion_original(String retroalimentacion_original) {
        this.retroalimentacion_original = retroalimentacion_original;
    }

    public ArrayList<HabilidadesAnalizadas> getHabilidades_analizadas() {
        return habilidades_analizadas;
    }

    public void setHabilidades_analizadas(ArrayList<HabilidadesAnalizadas> habilidades_analizadas) {
        this.habilidades_analizadas = habilidades_analizadas;
    }

    public ArrayList<MetodologiasSugeridas> getMetodologias_sugeridas() {
        return metodologias_sugeridas;
    }

    public void setMetodologias_sugeridas(ArrayList<MetodologiasSugeridas> metodologias_sugeridas) {
        this.metodologias_sugeridas = metodologias_sugeridas;
    }

    public MetadataAnalisis getMetadata_analisis() {
        return metadata_analisis;
    }

    public void setMetadata_analisis(MetadataAnalisis metadata_analisis) {
        this.metadata_analisis = metadata_analisis;
    }
}
