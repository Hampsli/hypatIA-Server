package com.hypatia.repository;

import com.hypatia.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OtpCode entity database operations.
 *
 * This repository manages One-Time Password (OTP) codes for secure authentication:
 * - Email-based verification for registration and login
 * - Password reset and account recovery
 * - Two-factor authentication enhancement
 * - Security audit and monitoring
 *
 * Security Features:
 * - Time-limited code validation
 * - Single-use verification enforcement
 * - Rate limiting support through usage tracking
 * - Automated cleanup of expired codes
 *
 * Performance Optimizations:
 * - Indexed email and code lookups
 * - Efficient expiration queries
 * - Bulk operations for maintenance
 * - Minimal data exposure for security
 *
 * @author hypatIA Development Team
 */
@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByEmailAndCode(String email, String code);

    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.isUsed = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC")
    List<OtpCode> findValidOtpsByEmail(@Param("email") String email, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.isUsed = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpCode> findMostRecentValidOtp(@Param("email") String email, @Param("currentTime") LocalDateTime currentTime);

    List<OtpCode> findByEmailOrderByCreatedAtDesc(String email);

    List<OtpCode> findByExpiresAtBefore(LocalDateTime currentTime);

    List<OtpCode> findByIsUsedTrue(); // For audit purposes
    List<OtpCode> findByIsUsedFalse(); // For monitoring unused

    /**
     * NEW: Counts OTPs that have been successfully used.
     * Needed for authentication statistics.
     */
    long countByIsUsedTrue();


    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.email = :email AND o.isUsed = false AND o.expiresAt > :currentTime")
    long countActiveOtpsByEmail(@Param("email") String email, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.email = :email AND o.createdAt BETWEEN :startTime AND :endTime")
    long countOtpsByEmailInTimeWindow(@Param("email") String email,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    @Modifying
    @Query("UPDATE OtpCode o SET o.isUsed = true WHERE o.id = :id")
    int markAsUsed(@Param("id") Long id); // Note: AuthService might use otpCode.markAsUsed() then save()

    @Modifying
    @Query("UPDATE OtpCode o SET o.isUsed = true WHERE o.email = :email AND o.isUsed = false")
    int markAllAsUsedForEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :currentTime")
    int deleteExpiredOtps(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.isUsed = true AND o.createdAt < :cutoffDate")
    int deleteOldUsedOtps(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT o FROM OtpCode o WHERE o.createdAt BETWEEN :startTime AND :endTime ORDER BY o.createdAt DESC")
    List<OtpCode> findOtpCodesInPeriod(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    @Query("SELECT FUNCTION('HOUR', o.createdAt) as hour, COUNT(o) as count " +
            "FROM OtpCode o " +
            "WHERE o.createdAt >= :startDate " +
            "GROUP BY FUNCTION('HOUR', o.createdAt) " +
            "ORDER BY hour")
    List<Object[]> getOtpUsageByHour(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o.email, COUNT(o) as requestCount " +
            "FROM OtpCode o " +
            "WHERE o.createdAt >= :timeWindow " +
            "GROUP BY o.email " +
            "HAVING COUNT(o) > :threshold " +
            "ORDER BY requestCount DESC")
    List<Object[]> findEmailsWithExcessiveRequests(@Param("threshold") long threshold,
                                                   @Param("timeWindow") LocalDateTime timeWindow);

    @Query("SELECT " +
            "COUNT(CASE WHEN o.isUsed = true THEN 1 END) as usedCount, " +
            "COUNT(o) as totalCount, " +
            "(COUNT(CASE WHEN o.isUsed = true THEN 1 END) * 100.0 / COUNT(o)) as successRate " +
            "FROM OtpCode o " +
            "WHERE o.createdAt >= :startDate")
    List<Object[]> getVerificationSuccessRate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM OtpCode o " +
            "WHERE o.email = :email AND o.isUsed = false AND o.expiresAt > :currentTime")
    boolean hasValidOtp(@Param("email") String email, @Param("currentTime") LocalDateTime currentTime);
}