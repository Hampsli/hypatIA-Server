package com.hypatia.service;

import com.hypatia.dto.UserProfileDto;
import com.hypatia.dto.UserRegistrationDto;
import com.hypatia.entity.User;
import com.hypatia.entity.UserProfile;
import com.hypatia.entity.UserStatus;
import com.hypatia.repository.UserProfileRepository;
import com.hypatia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.hypatia.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio que gestiona toda la lógica de negocio para Usuarios y sus Perfiles.
 * Implementa UserDetailsService para integrarse con Spring Security.
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        boolean enabled;
        if (user.getStatus() == UserStatus.PARTICIPANT || user.getStatus() == UserStatus.COACHING_READY) {
            enabled = true;
        } else {
            // PENDING_ONBOARDING implies they cannot fully log in yet
            enabled = false;
        }

        // Assume these are true by default for healthy accounts
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // Assuming Role_User is your enum for roles in the User entity
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),          // username
                user.getPassword(),       // password (encoded)
                enabled,                  // isEnabled
                accountNonExpired,        // isAccountNonExpired
                credentialsNonExpired,    // isCredentialsNonExpired
                accountNonLocked,         // isAccountNonLocked
                authorities               // authorities (roles/permissions)
        );
    }

    /**
     * Registers a new user in the system.
     * Creates the User entity and its UserProfile, including initial profile data.
     *
     * @param registrationDto DTO with email, password, name, age range, and current role.
     * @return The created User entity.
     * @throws IllegalArgumentException if the email is already registered.
     */
    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            log.warn("Registration attempt with already registered email: {}", registrationDto.getEmail());
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        User newUser = new User();
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        User savedUser = userRepository.save(newUser);
        log.info("New user registered with email: {}", savedUser.getEmail());

        // Create and save the user profile with initial registration data
        // Uses the UserProfile constructor that takes User, name, ageRange, currentRole
        UserProfile newProfile = new UserProfile(
                savedUser,
                registrationDto.getName(),
                registrationDto.getAgeRange(), // Correctly use getAgeRange() from DTO
                registrationDto.getCurrentRole()
        );
        userProfileRepository.save(newProfile);
        log.info("User profile created for user: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Saves or updates a User entity.
     * Necessary for AuthService to update user status.
     * @param user The User entity to save or update.
     * @return The saved/updated User entity.
     */
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        return userRepository.save(user);
    }

    /**
     * Updates the password for a given user.
     * @param user The user whose password needs to be updated.
     * @param newPassword The new plain text password.
     */
    public void updatePassword(User user, String newPassword) {
        log.info("Updating password for user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Verifies if an email is already registered in the system.
     * @param email The email to verify.
     * @return true if the email already exists, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("Checking if email {} exists: {}", email, exists);
        return exists;
    }

    /**
     * Retrieves the user profile. If the profile does not exist, an exception is thrown.
     * This method is used by other services (like PDFService) that require the profile.
     * @param user The user for whom the profile is sought.
     * @return The UserProfile associated with the user.
     * @throws UserNotFoundException if the user's profile is not found.
     */
    @Transactional(readOnly = true)
    public UserProfile getUserProfile(User user) {
        log.debug("Attempting to retrieve user profile for user ID: {}", user.getId());
        return userProfileRepository.findByUser(user).orElse(null);
    }



    /**
     * Retrieves a user's profile and packages it into a response for the controller.
     */
    public Map<String, Object> getUserProfileResponse(User user) {
        UserProfile profile = getUserProfile(user); // Use the dedicated getUserProfile method
        return buildProfileResponseMap(profile);
    }

    /**
     * Updates a user's profile from a DTO and returns the response.
     * If the profile becomes complete after the update, the user's status
     * changes from PENDING_ONBOARDING to PARTICIPANT.
     * @param user The user whose profile is to be updated.
     * @param profileDto The DTO with updated profile data.
     * @return A map containing the updated profile response.
     */
    public Map<String, Object> updateUserProfile(User user, UserProfileDto profileDto) {
        UserProfile profile = getUserProfile(user); // Use the dedicated getUserProfile method
        updateEntityFromDto(profile, profileDto); // Map data from DTO to entity
        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("User profile updated for user: {}", user.getEmail());

        // Check if the profile is complete and if the user's status is PENDING_ONBOARDING
        if (isProfileComplete(updatedProfile) && user.getStatus() == UserStatus.PENDING_ONBOARDING) {
            user.setStatus(UserStatus.PARTICIPANT); // Change status to PARTICIPANT
            userRepository.save(user); // Save the user with the updated status
            log.info("User {} status changed to PARTICIPANT after profile completion.", user.getEmail());
        }

        return buildProfileResponseMap(updatedProfile);
    }

    /**
     * Retrieves the profile completion status for a user.
     * @param user The user whose profile completion status is requested.
     * @return A map containing the completion status.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProfileCompletionStatus(User user) {
        UserProfile profile = getUserProfile(user); // Use the dedicated getUserProfile method
        return calculateCompletionStatus(profile);
    }

    /**
     * Retrieves the profile requirements (mandatory fields) for the frontend.
     * @return A map defining the required profile fields.
     */
    public Map<String, Object> getProfileRequirements() {
        // This logic defines the required fields for the frontend
        return Map.of(
                "requiredFields", List.of("name", "ageRange", "currentRole", "gender", "initialEducation", "yearsOfExperience", "desiredPosition",
                        "technologyLanguage", "startedInTech", "currentPosition",
                        "workMode", "salaryRange", "expectedSalary"),
                "minimumCompletionPercentage", 75
        );
    }

    // --- Private Helper Methods ---

    /**
     * Constructs the response map for the user profile.
     * @param profile The UserProfile.
     * @return A map containing the profile DTO, its completeness status, and completion details.
     */
    private Map<String, Object> buildProfileResponseMap(UserProfile profile) {
        return Map.of(
                "profile", toUserProfileDto(profile),
                "isComplete", isProfileComplete(profile),
                "completionStatus", calculateCompletionStatus(profile)
        );
    }

    /**
     * Maps data from a UserProfileDto to an existing UserProfile entity.
     * Only updates fields that are present in the DTO (non-null).
     *
     * @param profile The UserProfile entity to update.
     * @param profileDto The DTO with the updated data.
     */
    private void updateEntityFromDto(UserProfile profile, UserProfileDto profileDto) {
        // Update name, ageRange, currentRole from DTO as they are now in UserProfile
        if (profileDto.getName() != null) {
            profile.setName(profileDto.getName());
        }
        if (profileDto.getAgeRange() != null) { // Correctly update ageRange (String)
            profile.setAgeRange(profileDto.getAgeRange());
        }


        // Rest of UserProfileDto mappings
        if (profileDto.getGender() != null) {
            profile.setGender(profileDto.getGender());
        }
        if (profileDto.getCvPath() != null) {
            profile.setCvPath(profileDto.getCvPath());
        }
        if (profileDto.getIsCaregiver() != null) {
            profile.setIsCaregiver(profileDto.getIsCaregiver());
        }
        if (profileDto.getCaregivingHoursPerWeek() != null) {
            profile.setCaregivingHoursPerWeek(profileDto.getCaregivingHoursPerWeek());
        }
        if (profileDto.getInitialEducation() != null) {
            profile.setInitialEducation(profileDto.getInitialEducation());
        }
        if (profileDto.getHigherEducationArea() != null) {
            profile.setHigherEducationArea(profileDto.getHigherEducationArea());
        }
        if (profileDto.getTechnologyLanguage() != null) {
            profile.setTechnologyLanguage(profileDto.getTechnologyLanguage());
        }
        if (profileDto.getYearsOfExperience() != null) {
            profile.setYearsOfExperience(profileDto.getYearsOfExperience());
        }
        if (profileDto.getStartedInTech() != null) {
            profile.setStartedInTech(profileDto.getStartedInTech());
        }
        if (profileDto.getCurrentPosition() != null) {
            profile.setCurrentPosition(profileDto.getCurrentPosition());
        }
        if (profileDto.getWorkMode() != null) {
            profile.setWorkMode(profileDto.getWorkMode());
        }
        if (profileDto.getSalaryRange() != null) {
            profile.setSalaryRange(profileDto.getSalaryRange());
        }
        if (profileDto.getReasonsForMovement() != null) {
            // Assuming profileDto.getReasonsForMovement() now returns List<String>
            profile.setReasonsForMovement(profileDto.getReasonsForMovement());
        }
        if (profileDto.getExpectedSalary() != null) {
            profile.setExpectedSalary(profileDto.getExpectedSalary());
        }
        if (profileDto.getHasCompletedCourses() != null) {
            profile.setHasCompletedCourses(profileDto.getHasCompletedCourses());
        }
        if (profileDto.getProjectsBuilt() != null) {
            profile.setProjectsBuilt(profileDto.getProjectsBuilt());
        }
        if (profileDto.getLastFeedback() != null) {
            profile.setLastFeedback(profileDto.getLastFeedback());
        }
        if (profileDto.getDesiredPosition() != null) {
            profile.setDesiredPosition(profileDto.getDesiredPosition());
        }
        if (profileDto.getTargetJobs() != null) {
            // Assuming profileDto.getTargetJobs() now returns List<String>
            profile.setTargetJobs(profileDto.getTargetJobs());
        }
        if (profileDto.getDailyTasks() != null) {
            profile.setDailyTasks(profileDto.getDailyTasks());
        }
        if (profileDto.getSoftSkills() != null) {
            profile.setSoftSkills(profileDto.getSoftSkills());
        }
    }

    /**
     * Converts a UserProfile entity to a UserProfileDto.
     * @param profile The UserProfile entity to convert.
     * @return The resulting UserProfileDto.
     */
    public UserProfileDto toUserProfileDto(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(profile.getUser().getId());
        // Map name, ageRange, currentRole from UserProfile to UserProfileDto
        dto.setName(profile.getName());
        dto.setAgeRange(profile.getAgeRange()); // Use getAgeRange()


        // Rest of UserProfile to DTO mappings
        dto.setGender(profile.getGender());
        dto.setCvPath(profile.getCvPath());
        dto.setIsCaregiver(profile.getIsCaregiver());
        dto.setCaregivingHoursPerWeek(profile.getCaregivingHoursPerWeek());
        dto.setInitialEducation(profile.getInitialEducation());
        dto.setHigherEducationArea(profile.getHigherEducationArea());
        dto.setTechnologyLanguage(profile.getTechnologyLanguage());
        dto.setYearsOfExperience(profile.getYearsOfExperience());
        dto.setStartedInTech(profile.getStartedInTech());
        dto.setCurrentPosition(profile.getCurrentPosition());
        dto.setWorkMode(profile.getWorkMode());
        dto.setSalaryRange(profile.getSalaryRange());
        dto.setReasonsForMovement(profile.getReasonsForMovement());
        dto.setExpectedSalary(profile.getExpectedSalary());
        dto.setHasCompletedCourses(profile.getHasCompletedCourses());
        dto.setProjectsBuilt(profile.getProjectsBuilt());
        dto.setLastFeedback(profile.getLastFeedback());
        dto.setDesiredPosition(profile.getDesiredPosition());
        dto.setTargetJobs(profile.getTargetJobs());
        dto.setDailyTasks(profile.getDailyTasks());
        dto.setSoftSkills(profile.getSoftSkills());
        dto.setCaregiverStatus(profile.getCaregiverStatus());
        dto.setMaxHoursPerWeek(profile.getMaxHoursPerWeek());
        return dto;
    }

    /**
     * Calculates if the user profile is complete based on defined criteria.
     *
     * @param profile The UserProfile to evaluate.
     * @return true if the profile is complete, false otherwise.
     */
    private boolean isProfileComplete(UserProfile profile) {
        // Check for name, ageRange, currentRole as mandatory in UserProfile
        return profile.getName() != null && !profile.getName().isEmpty() &&
                profile.getAgeRange() != null && !profile.getAgeRange().isEmpty() && // Check ageRange
                profile.getGender() != null && !profile.getGender().isEmpty() &&
                profile.getInitialEducation() != null && !profile.getInitialEducation().isEmpty() &&
                profile.getYearsOfExperience() != null && !profile.getYearsOfExperience().isEmpty() &&
                profile.getDesiredPosition() != null && !profile.getDesiredPosition().isEmpty() &&
                profile.getTechnologyLanguage() != null && !profile.getTechnologyLanguage().isEmpty() &&
                profile.getStartedInTech() != null && !profile.getStartedInTech().isEmpty() &&
                profile.getCurrentPosition() != null && !profile.getCurrentPosition().isEmpty() &&
                profile.getWorkMode() != null && !profile.getWorkMode().isEmpty() &&
                profile.getSalaryRange() != null && !profile.getSalaryRange().isEmpty() &&
                profile.getExpectedSalary() != null && !profile.getExpectedSalary().isEmpty();
    }

    /**
     * Calculates the profile completion status, including a percentage.
     *
     * @param profile The UserProfile to evaluate.
     * @return A map with the completion status (e.g., percentage, missing fields).
     */
    private Map<String, Object> calculateCompletionStatus(UserProfile profile) {
        // Include name, ageRange, currentRole in important fields
        List<String> importantFields = List.of(
                "name", "ageRange", "currentRole", "gender", "cvPath", "isCaregiver", "caregivingHoursPerWeek",
                "initialEducation", "higherEducationArea", "technologyLanguage",
                "yearsOfExperience", "startedInTech", "currentPosition",
                "workMode", "salaryRange", "expectedSalary", "hasCompletedCourses",
                "projectsBuilt", "lastFeedback", "desiredPosition",
                "dailyTasks", "softSkills", "reasonsForMovement", "targetJobs"
        );

        int totalImportantFields = importantFields.size();
        int completedImportantFields = 0;
        List<String> missingFields = new ArrayList<>();

        // Updated checks for name, ageRange, currentRole
        if (profile.getName() != null && !profile.getName().isEmpty()) completedImportantFields++; else missingFields.add("name");
        if (profile.getAgeRange() != null && !profile.getAgeRange().isEmpty()) completedImportantFields++; else missingFields.add("ageRange");


        if (profile.getGender() != null && !profile.getGender().isEmpty()) completedImportantFields++; else missingFields.add("gender");
        if (profile.getCvPath() != null && !profile.getCvPath().isEmpty()) completedImportantFields++; else missingFields.add("cvPath");
        if (profile.getIsCaregiver() != null) completedImportantFields++; else missingFields.add("isCaregiver");
        if (profile.getCaregivingHoursPerWeek() != null && !profile.getCaregivingHoursPerWeek().isEmpty()) completedImportantFields++; else missingFields.add("caregivingHoursPerWeek");
        if (profile.getInitialEducation() != null && !profile.getInitialEducation().isEmpty()) completedImportantFields++; else missingFields.add("initialEducation");
        if (profile.getHigherEducationArea() != null && !profile.getHigherEducationArea().isEmpty()) completedImportantFields++; else missingFields.add("higherEducationArea");
        if (profile.getTechnologyLanguage() != null && !profile.getTechnologyLanguage().isEmpty()) completedImportantFields++; else missingFields.add("technologyLanguage");
        if (profile.getYearsOfExperience() != null && !profile.getYearsOfExperience().isEmpty()) completedImportantFields++; else missingFields.add("yearsOfExperience");
        if (profile.getStartedInTech() != null && !profile.getStartedInTech().isEmpty()) completedImportantFields++; else missingFields.add("startedInTech");
        if (profile.getCurrentPosition() != null && !profile.getCurrentPosition().isEmpty()) completedImportantFields++; else missingFields.add("currentPosition");
        if (profile.getWorkMode() != null && !profile.getWorkMode().isEmpty()) completedImportantFields++; else missingFields.add("workMode");
        if (profile.getSalaryRange() != null && !profile.getSalaryRange().isEmpty()) completedImportantFields++; else missingFields.add("salaryRange");
        if (profile.getExpectedSalary() != null && !profile.getExpectedSalary().isEmpty()) completedImportantFields++; else missingFields.add("expectedSalary");
        if (profile.getHasCompletedCourses() != null) completedImportantFields++; else missingFields.add("hasCompletedCourses");
        if (profile.getProjectsBuilt() != null) completedImportantFields++; else missingFields.add("projectsBuilt");
        if (profile.getLastFeedback() != null && !profile.getLastFeedback().isEmpty()) completedImportantFields++; else missingFields.add("lastFeedback");
        if (profile.getDesiredPosition() != null && !profile.getDesiredPosition().isEmpty()) completedImportantFields++; else missingFields.add("desiredPosition");
        if (profile.getDailyTasks() != null && !profile.getDailyTasks().isEmpty()) completedImportantFields++; else missingFields.add("dailyTasks");
        if (profile.getSoftSkills() != null && !profile.getSoftSkills().isEmpty()) completedImportantFields++; else missingFields.add("softSkills");
        if (profile.getReasonsForMovement() != null && !profile.getReasonsForMovement().isEmpty()) completedImportantFields++; else missingFields.add("reasonsForMovement"); // Lists
        if (profile.getTargetJobs() != null && !profile.getTargetJobs().isEmpty()) completedImportantFields++; else missingFields.add("targetJobs"); // Lists

        double completionPercentage = (totalImportantFields > 0) ? (double) completedImportantFields / totalImportantFields * 100 : 0;

        Map<String, Object> status = new HashMap<>();
        status.put("percentage", Math.round(completionPercentage));
        status.put("completedFields", completedImportantFields);
        status.put("totalFields", totalImportantFields);
        status.put("missingFields", missingFields);
        status.put("message", String.format("Tu perfil está completo en %.0f%%.", completionPercentage));

        return status;
    }
}