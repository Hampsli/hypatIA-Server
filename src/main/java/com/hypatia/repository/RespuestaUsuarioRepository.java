package com.hypatia.repository;

import com.hypatia.entity.RespuestaUsuario;
import com.hypatia.entity.User; // Keep this import for clarity if User object is passed
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Explicitly import Param
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad RespuestaUsuario.
 *
 * Gestiona el almacenamiento y la recuperación de las respuestas de los usuarios a los cuestionarios.
 * Proporciona métodos de acceso a datos que serán utilizados por la capa de servicio
 * para realizar cálculos de progreso, puntuaciones y otros análisis.
 */
@Repository
public interface RespuestaUsuarioRepository extends JpaRepository<RespuestaUsuario, Long> {

    /**
     * Busca la respuesta más reciente de un usuario a una pregunta específica.
     *
     * @param userId El ID del usuario.
     * @param preguntaId El ID de la pregunta.
     * @return Un Optional que contiene la respuesta si existe.
     */
    Optional<RespuestaUsuario> findTopByUser_IdAndPregunta_IdOrderByCreatedAtDesc(Long userId, Long preguntaId);

    /**
     * Busca todas las respuestas (solo la versión más reciente por pregunta) de un usuario
     * para un cuestionario específico identificado por su fase.
     *
     * @param userId El ID del usuario.
     * @param fase La fase del cuestionario (ej. "onboarding").
     * @return Una lista de las respuestas más recientes del usuario para ese cuestionario.
     */
    @Query("SELECT r FROM RespuestaUsuario r WHERE r.user.id = :userId " +
            "AND r.pregunta.cuestionario.faseCuestionario = :fase AND r.isCurrent = true")
    List<RespuestaUsuario> findCurrentUserResponsesByPhase(@Param("userId") Long userId, @Param("fase") String fase);

    /**
     * NEW: Busca todas las respuestas actuales de un usuario,
     * y eager-fetches la Pregunta y sus Opciones.
     * Esto es crucial para PDFService/QuestionnaireService.getUserQuestionsAndAnswers
     * para evitar LazyInitializationException.
     *
     * @param userId El ID del usuario.
     * @return Lista de respuestas actuales con detalles de pregunta y opciones.
     */
    @Query("SELECT r FROM RespuestaUsuario r JOIN FETCH r.pregunta p LEFT JOIN FETCH p.opciones o WHERE r.user.id = :userId AND r.isCurrent = true ORDER BY p.orden ASC")
    List<RespuestaUsuario> findCurrentUserResponsesWithDetails(@Param("userId") Long userId);


    /**
     * Cuenta las respuestas actuales de un usuario para un cuestionario específico.
     *
     * @param userId El ID del usuario.
     * @param fase La fase del cuestionario.
     * @return El número de preguntas que el usuario ha respondido.
     */
    long countByUser_IdAndPregunta_Cuestionario_FaseCuestionarioAndIsCurrentTrue(Long userId, String fase);

    /**
     * Marca todas las respuestas de un usuario para una fase como "no actuales".
     * Se usa cuando un usuario decide reiniciar un cuestionario.
     *
     * @param userId El ID del usuario.
     * @param fase La fase del cuestionario.
     */
    @Modifying
    @Query("UPDATE RespuestaUsuario r SET r.isCurrent = false " +
            "WHERE r.user.id = :userId AND r.pregunta.cuestionario.faseCuestionario = :fase")
    int markAllResponsesAsNotCurrent(@Param("userId") Long userId, @Param("fase") String fase); // Changed return type to int for count of updated records

    /**
     * NEW: Marca como "no actuales" las respuestas anteriores de un usuario para un conjunto específico de preguntas.
     * Utilizado para asegurar que solo una respuesta sea "current" por pregunta/usuario.
     *
     * @param userId El ID del usuario.
     * @param preguntaIds IDs de las preguntas cuyas respuestas se deben marcar como no actuales.
     */
    @Modifying
    @Query("UPDATE RespuestaUsuario r SET r.isCurrent = false " +
            "WHERE r.user.id = :userId AND r.pregunta.id IN :preguntaIds")
    void markResponsesAsNotCurrentForQuestionIds(@Param("userId") Long userId, @Param("preguntaIds") List<Long> preguntaIds);


    /**
     * Obtiene la distribución de respuestas para una pregunta específica.
     * Devuelve una lista de arrays, donde cada array contiene el texto de la respuesta
     * y el número de usuarios que eligieron esa respuesta.
     *
     * @param preguntaId El ID de la pregunta.
     * @return Una lista de Object[] con [respuestaTexto, conteo].
     */
    @Query("SELECT r.respuestaTexto, COUNT(r) FROM RespuestaUsuario r " +
            "WHERE r.pregunta.id = :preguntaId AND r.isCurrent = true GROUP BY r.respuestaTexto")
    List<Object[]> getResponseDistributionForQuestion(@Param("preguntaId") Long preguntaId);
}