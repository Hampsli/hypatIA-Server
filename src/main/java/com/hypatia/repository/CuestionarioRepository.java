package com.hypatia.repository;

import com.hypatia.entity.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Cuestionario.
 * Gestiona las operaciones de la base de datos para los cuestionarios.
 */
@Repository
public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {

    /**
     * Busca un cuestionario activo por su nombre de fase.
     * Este método es clave para iniciar un cuestionario en la aplicación.
     *
     * @param fase El nombre de la fase del cuestionario (ej. "onboarding").
     * @return Un Optional que contiene el Cuestionario si se encuentra y está activo.
     */
    Optional<Cuestionario> findByFaseCuestionarioAndActivoIsTrue(String fase);

    /**
     * Alternativa con JPQL para cargar el cuestionario con todas sus relaciones (secciones, preguntas, opciones)
     * de una sola vez para evitar problemas de carga perezosa (LazyInitializationException).
     *
     * @param fase El nombre de la fase del cuestionario.
     * @return Un Optional que contiene el Cuestionario con toda su estructura cargada.
     */
    @Query("SELECT c FROM Cuestionario c " +
            "LEFT JOIN FETCH c.secciones s " +
            "LEFT JOIN FETCH s.preguntas p " +
            "LEFT JOIN FETCH p.opciones o " +
            "WHERE c.faseCuestionario = :fase AND c.activo = true")
    Optional<Cuestionario> findFullQuestionnaireByPhase(String fase);
}