package com.hypatia.repository;

import com.hypatia.entity.AiInteraction;
import com.hypatia.entity.User;
import com.hypatia.Constants.Queries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Optimized Repository interface for AiInteraction entity operations.
 * Now supporting Spring-side caching.
 *
 * @author hypatIA Development Team
 */
@Repository
public interface AiInteractionRepository extends JpaRepository<AiInteraction, Long> {

    List<AiInteraction> findByUserOrderByCreatedAtDesc(User user);

    List<AiInteraction> findByInteractionTypeOrderByCreatedAtDesc(String interactionType);

    @Query("SELECT ai FROM AiInteraction ai " +
            "WHERE ai.user = :user " +
            "AND ai.createdAt >= :since " +
            "ORDER BY ai.createdAt DESC " +
            "LIMIT :limit")
    List<AiInteraction> findRecentInteractionsByUser(@Param("user") User user,
                                                     @Param("since") LocalDateTime since,
                                                     @Param("limit") int limit);

    @Query(value=Queries.QUERY_GET_AI_INTERACTIONS_BY_USER_ID, nativeQuery = true)
   Optional<AiInteraction>  findLastInteractionByUserId(@Param("userID") Long userID);

    long countByUser(User user);

    @Query("SELECT ai FROM AiInteraction ai " +
            "WHERE ai.user = :user " +
            "AND ai.interactionType = :interactionType " +
            "ORDER BY ai.createdAt DESC " +
            "LIMIT 1")
    Optional<AiInteraction> findLatestInteractionByUserAndType(@Param("user") User user,
                                                               @Param("interactionType") String interactionType);

    @Modifying
    @Query("DELETE FROM AiInteraction ai WHERE ai.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    long count();

    @Query("SELECT ai.interactionType, COUNT(ai) FROM AiInteraction ai GROUP BY ai.interactionType ORDER BY COUNT(ai) DESC")
    List<Object[]> countInteractionsByType();

    @Query("SELECT ai.user.id, COUNT(ai) as interactionCount " +
            "FROM AiInteraction ai " +
            "GROUP BY ai.user.id " +
            "ORDER BY interactionCount DESC " +
            "LIMIT :limit")
    List<Object[]> findMostActiveAiUsers(@Param("limit") int limit);

    long countByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    // --- New/Adapted Methods for Caching ---

    /**
     * Finds a valid cached AI response by its unique cache key.
     * This method is used by Spring's @Cacheable mechanism.
     * In a real caching scenario, you might have an in-memory cache
     * managed by Spring that stores the actual AIResponseDto.
     * This DB query serves as a persistent cache or a fallback if in-memory fails.
     *
     * @param cacheKey The unique hash/key of the AI request.
     * @return An Optional containing the AiInteraction if a match is found and is valid for caching.
     */
    Optional<AiInteraction> findByCacheKey(String cacheKey);

    /**
     * Counts interactions that were served from cache.
     */
    long countByIsCachedResponseTrue();

    /**
     * Counts interactions that were not served from cache (i.e., fresh API calls).
     */
    long countByIsCachedResponseFalse();
}