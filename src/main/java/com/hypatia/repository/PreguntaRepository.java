package com.hypatia.repository;

import com.hypatia.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Pregunta.
 * Gestiona las operaciones de la base de datos para las preguntas de los cuestionarios.
 */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    /**
     * Busca todas las preguntas asociadas a una sección específica, ordenadas.
     * Reemplaza la antigua búsqueda por "categoría".
     *
     * @param seccionId El ID de la CuestionarioSeccion.
     * @return Una lista de preguntas para esa sección.
     */
    List<Pregunta> findBySeccionIdOrderByOrdenAsc(Long seccionId);

    /**
     * Cuenta el número total de preguntas para un cuestionario específico.
     * Útil para calcular el progreso.
     *
     * @param cuestionarioId El ID del Cuestionario.
     * @return El número total de preguntas.
     */
    long countByCuestionarioId(Long cuestionarioId);

    /**
     * Cuenta el número total de preguntas para un cuestionario específico,
     * buscado por la fase del cuestionario.
     *
     * @param fase La fase del Cuestionario (ej. "onboarding").
     * @return El número total de preguntas.
     */
    long countByCuestionario_FaseCuestionario(String fase);

    /**
     * Busca preguntas por texto para funciones de administración.
     *
     * @param searchText El texto a buscar dentro de las preguntas.
     * @return Una lista de preguntas que coinciden.
     */
    List<Pregunta> findByTextoPreguntaContainingIgnoreCase(String searchText);

    /**
     * Obtiene la fase del cuestionario a la que pertenece una pregunta.
     *
     * @param preguntaId El ID de la pregunta.
     * @return El string de la fase del cuestionario.
     */
    @Query("SELECT p.cuestionario.faseCuestionario FROM Pregunta p WHERE p.id = :preguntaId")
    String findFaseCuestionarioById(Long preguntaId);
}