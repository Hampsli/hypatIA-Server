package com.hypatia.repository;

import com.hypatia.entity.UserProfile;
import com.hypatia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserProfile entity database operations.
 * 
 * This repository manages comprehensive user profile data for the hypatIA platform:
 * - Detailed professional and personal information
 * - Career development tracking and analytics
 * - Skills assessment and gap analysis
 * - Work-life balance and caregiver considerations
 * - Educational background and experience mapping
 * 
 * Profile Management Features:
 * - Complete profile lifecycle operations
 * - Advanced search and filtering capabilities
 * - Analytics and reporting for platform insights
 * - Mentorship matching and peer connections
 * - AI-powered recommendations and personalization
 * 
 * Data Privacy and Security:
 * - Secure handling of sensitive personal information
 * - GDPR compliance for profile data management
 * - Controlled access to caregiver and personal data
 * - Audit trails for profile modifications
 * 
 * @author hypatIA Development Team
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Finds a user profile by associated user.
     * 
     * Primary profile lookup for:
     * - User authentication and session management
     * - Profile display and editing
     * - AI personalization and recommendations
     * - Assessment and career guidance
     * 
     * @param user User entity
     * @return Optional containing profile if exists
     */
    Optional<UserProfile> findByUser(User user);

    /**
     * Finds a user profile by user ID.
     * 
     * Optimized lookup for:
     * - API endpoint responses
     * - Service layer operations
     * - Caching and performance optimization
     * - Cross-service profile access
     * 
     * @param userId User's unique identifier
     * @return Optional containing profile if exists
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Checks if a profile exists for a user.
     * 
     * Profile completion validation:
     * - Onboarding flow management
     * - Profile setup requirements
     * - User activation tracking
     * - Feature access control
     * 
     * @param userId User's unique identifier
     * @return true if profile exists, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Finds profiles by gender for demographic analysis.
     * 
     * Demographic insights and analytics:
     * - Gender representation in STEM
     * - Targeted program development
     * - Diversity and inclusion metrics
     * - Platform impact assessment
     * 
     * @param gender Gender identity
     * @return List of profiles matching gender
     */
    List<UserProfile> findByGender(String gender);

    /**
     * Finds profiles by caregiver status.
     * 
     * Work-life balance analysis:
     * - Caregiver support program targeting
     * - Flexible work arrangement recommendations
     * - Career progression pattern analysis
     * - Platform feature customization
     * 
     * @param isCaregiver Caregiver status
     * @return List of profiles with matching caregiver status
     */
    List<UserProfile> findByIsCaregiver(Boolean isCaregiver);

    /**
     * Finds profiles by initial education level.
     * 
     * Educational background analysis:
     * - Career pathway recommendations
     * - Skills development prioritization
     * - Educational resource targeting
     * - Professional development planning
     * 
     * @param initialEducation Education level
     * @return List of profiles with matching education level
     */
    List<UserProfile> findByInitialEducation(String initialEducation);

    /**
     * Finds profiles by higher education area.
     * 
     * Academic specialization analysis:
     * - Field-specific career guidance
     * - Skills transferability assessment
     * - Industry transition opportunities
     * - Peer networking and mentorship
     * 
     * @param higherEducationArea Area of study
     * @return List of profiles with matching education area
     */
    List<UserProfile> findByHigherEducationArea(String higherEducationArea);

    /**
     * Finds profiles by technology language/framework.
     * 
     * Technical skills analysis:
     * - Technology-specific job matching
     * - Skills gap identification
     * - Learning path recommendations
     * - Technical mentorship connections
     * 
     * @param technologyLanguage Primary technology skill
     * @return List of profiles with matching technology
     */
    List<UserProfile> findByTechnologyLanguage(String technologyLanguage);

    /**
     * Finds profiles by years of experience.
     * 
     * Experience-based categorization:
     * - Seniority-appropriate opportunities
     * - Career progression analysis
     * - Mentorship role assignment
     * - Compensation benchmarking
     * 
     * @param yearsOfExperience Experience level category
     * @return List of profiles with matching experience
     */
    List<UserProfile> findByYearsOfExperience(String yearsOfExperience);

    /**
     * Finds profiles by current position.
     * 
     * Role-based analysis and networking:
     * - Position-specific insights
     * - Career advancement opportunities
     * - Peer group identification
     * - Industry trend analysis
     * 
     * @param currentPosition Job title or role
     * @return List of profiles with matching position
     */
    List<UserProfile> findByCurrentPosition(String currentPosition);

    /**
     * Finds profiles by preferred work mode.
     * 
     * Work arrangement preferences:
     * - Remote/hybrid opportunity matching
     * - Work-life balance optimization
     * - Geographic flexibility analysis
     * - Company culture alignment
     * 
     * @param workMode Work arrangement preference
     * @return List of profiles with matching work mode
     */
    List<UserProfile> findByWorkMode(String workMode);

    /**
     * Finds profiles by salary range.
     * 
     * Compensation analysis and benchmarking:
     * - Market rate analysis
     * - Pay equity assessment
     * - Career progression planning
     * - Negotiation support data
     * 
     * @param salaryRange Current salary bracket
     * @return List of profiles in salary range
     */
    List<UserProfile> findBySalaryRange(String salaryRange);

    /**
     * Finds profiles by desired position.
     * 
     * Career aspiration analysis:
     * - Goal-oriented career guidance
     * - Skills gap identification
     * - Learning path recommendations
     * - Mentorship opportunity matching
     * 
     * @param desiredPosition Target career goal
     * @return List of profiles with matching goals
     */
    List<UserProfile> findByDesiredPosition(String desiredPosition);

    /**
     * Finds profiles with course completion experience.
     * 
     * Learning engagement analysis:
     * - Continuous learning commitment
     * - Professional development patterns
     * - Certification pathway recommendations
     * - Educational resource effectiveness
     * 
     * @param hasCompletedCourses Course completion status
     * @return List of profiles with matching course history
     */
    List<UserProfile> findByHasCompletedCourses(Boolean hasCompletedCourses);

    /**
     * Finds profiles updated within date range.
     * 
     * Profile activity tracking:
     * - Recent profile updates
     * - User engagement monitoring
     * - Data freshness analysis
     * - Platform usage patterns
     * 
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of recently updated profiles
     */
    List<UserProfile> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds profiles by project count range.
     * 
     * Portfolio strength assessment:
     * - Hands-on experience evaluation
     * - Project-based skill development
     * - Portfolio completeness analysis
     * - Practical experience benchmarking
     * 
     * @param minProjects Minimum project count
     * @param maxProjects Maximum project count
     * @return List of profiles within project range
     */
    List<UserProfile> findByProjectsBuiltBetween(Integer minProjects, Integer maxProjects);

    /**
     * Advanced search for mentorship matching.
     * 
     * Mentorship program optimization:
     * - Experience-based mentor identification
     * - Technology and role alignment
     * - Geographic and schedule compatibility
     * - Mutual benefit assessment
     * 
     * @param experienceLevel Target experience level
     * @param technologyLanguage Shared technology interest
     * @param workMode Compatible work arrangement
     * @return List of potential mentorship matches
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "up.yearsOfExperience = :experienceLevel AND " +
           "up.technologyLanguage = :technologyLanguage AND " +
           "up.workMode = :workMode")
    List<UserProfile> findMentorshipCandidates(@Param("experienceLevel") String experienceLevel,
                                              @Param("technologyLanguage") String technologyLanguage,
                                              @Param("workMode") String workMode);

    /**
     * Finds profiles for career transition analysis.
     * 
     * Career change support:
     * - Transition pathway identification
     * - Skills transferability assessment
     * - Industry change opportunities
     * - Support resource targeting
     * 
     * @param currentPosition Current role
     * @param desiredPosition Target role
     * @return List of profiles with similar transitions
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "up.currentPosition = :currentPosition AND " +
           "up.desiredPosition = :desiredPosition")
    List<UserProfile> findCareerTransitionProfiles(@Param("currentPosition") String currentPosition,
                                                   @Param("desiredPosition") String desiredPosition);

    /**
     * Finds profiles needing skills development.
     * 
     * Learning and development targeting:
     * - Skills gap identification
     * - Training program recommendations
     * - Professional development opportunities
     * - Career advancement support
     * 
     * @param currentLevel Current experience level
     * @return List of profiles needing development
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "up.yearsOfExperience = :currentLevel AND " +
           "up.desiredPosition IS NOT NULL")
    List<UserProfile> findProfilesNeedingDevelopment(@Param("currentLevel") String currentLevel);

    /**
     * Gets technology distribution statistics.
     * 
     * Technology landscape analysis:
     * - Popular technology adoption
     * - Skills market demand
     * - Learning path optimization
     * - Industry trend identification
     * 
     * @return List of technology counts
     */
    @Query("SELECT up.technologyLanguage, COUNT(up) FROM UserProfile up " +
           "WHERE up.technologyLanguage IS NOT NULL " +
           "GROUP BY up.technologyLanguage " +
           "ORDER BY COUNT(up) DESC")
    List<Object[]> getTechnologyDistribution();

    /**
     * Gets work mode preferences statistics.
     * 
     * Work arrangement insights:
     * - Remote work adoption trends
     * - Flexibility preferences
     * - Geographic distribution impact
     * - Company policy alignment
     * 
     * @return List of work mode preferences
     */
    @Query("SELECT up.workMode, COUNT(up) FROM UserProfile up " +
           "WHERE up.workMode IS NOT NULL " +
           "GROUP BY up.workMode " +
           "ORDER BY COUNT(up) DESC")
    List<Object[]> getWorkModeDistribution();

    /**
     * Gets caregiver statistics by age group.
     * 
     * Work-life balance analysis:
     * - Caregiver demographics
     * - Age-related caregiving patterns
     * - Support program targeting
     * - Policy development insights
     * 
     * @return List of caregiver statistics by age
     */
    @Query("SELECT " +
           "CASE WHEN u.age BETWEEN 16 AND 25 THEN '16-25' " +
           "     WHEN u.age BETWEEN 26 AND 35 THEN '26-35' " +
           "     WHEN u.age BETWEEN 36 AND 45 THEN '36-45' " +
           "     WHEN u.age BETWEEN 46 AND 55 THEN '46-55' " +
           "     ELSE '55+' END as ageGroup, " +
           "up.isCaregiver, COUNT(up) " +
           "FROM UserProfile up JOIN up.user u " +
           "GROUP BY " +
           "CASE WHEN u.age BETWEEN 16 AND 25 THEN '16-25' " +
           "     WHEN u.age BETWEEN 26 AND 35 THEN '26-35' " +
           "     WHEN u.age BETWEEN 36 AND 45 THEN '36-45' " +
           "     WHEN u.age BETWEEN 46 AND 55 THEN '46-55' " +
           "     ELSE '55+' END, up.isCaregiver " +
           "ORDER BY ageGroup, up.isCaregiver")
    List<Object[]> getCaregiverStatsByAgeGroup();

    /**
     * Finds profiles for salary analysis.
     * 
     * Compensation equity research:
     * - Pay gap analysis
     * - Market rate benchmarking
     * - Career progression impact
     * - Negotiation support data
     * 
     * @param educationArea Educational background
     * @param experienceLevel Experience category
     * @return List of profiles for salary comparison
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "up.higherEducationArea = :educationArea AND " +
           "up.yearsOfExperience = :experienceLevel AND " +
           "up.salaryRange IS NOT NULL")
    List<UserProfile> findSalaryComparisonProfiles(@Param("educationArea") String educationArea,
                                                   @Param("experienceLevel") String experienceLevel);

    /**
     * Finds incomplete profiles for onboarding follow-up.
     * 
     * Profile completion optimization:
     * - Onboarding flow improvement
     * - User activation campaigns
     * - Feature adoption tracking
     * - Platform engagement enhancement
     * 
     * @return List of profiles with missing key information
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "up.currentPosition IS NULL OR " +
           "up.desiredPosition IS NULL OR " +
           "up.technologyLanguage IS NULL OR " +
           "up.yearsOfExperience IS NULL")
    List<UserProfile> findIncompleteProfiles();

    /**
     * Finds profiles for AI training data.
     * 
     * Machine learning and AI optimization:
     * - Training data collection
     * - Model improvement
     * - Personalization enhancement
     * - Recommendation algorithm training
     * 
     * @return List of complete profiles suitable for AI training
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "up.currentPosition IS NOT NULL AND " +
           "up.desiredPosition IS NOT NULL AND " +
           "up.technologyLanguage IS NOT NULL AND " +
           "up.yearsOfExperience IS NOT NULL AND " +
           "up.softSkills IS NOT NULL")
    List<UserProfile> findCompleteProfilesForAI();
}
