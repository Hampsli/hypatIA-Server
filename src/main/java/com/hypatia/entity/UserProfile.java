package com.hypatia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "name", length = 100, nullable = false)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    // --- OPTIMIZATION: Replaced 'age' (Integer) with 'age_range' (String) ---
    @Column(name = "age_range", length = 50, nullable = false) // Use consistent DB column name
    @NotBlank(message = "Age range is required")
    @Size(max = 50, message = "Age range must not exceed 50 characters")
    private String ageRange; // Field name in Java, maps to age_range in DB



    @Column(name = "gender", length = 50)
    @Size(max = 50, message = "Gender field must not exceed 50 characters")
    private String gender;

    @Column(name = "cv_path", length = 500)
    @Size(max = 500, message = "CV path must not exceed 500 characters")
    private String cvPath;

    @Column(name = "is_caregiver")
    private Boolean isCaregiver;

    @Column(name = "caregiving_hours_per_week", length = 50)
    @Size(max = 50, message = "Caregiving hours must not exceed 50 characters")
    private String caregivingHoursPerWeek;

    @Column(name = "initial_education", length = 50)
    @Size(max = 50, message = "Initial education must not exceed 50 characters")
    private String initialEducation;

    @Column(name = "higher_education_area", length = 100)
    @Size(max = 100, message = "Higher education area must not exceed 100 characters")
    private String higherEducationArea;

    @Column(name = "technology_language", length = 100)
    @Size(max = 100, message = "Technology language must not exceed 100 characters")
    private String technologyLanguage;

    @Column(name = "years_of_experience", length = 20)
    @Size(max = 20, message = "Years of experience must not exceed 20 characters")
    private String yearsOfExperience;

    @Column(name = "started_in_tech", length = 100)
    @Size(max = 100, message = "Started in tech must not exceed 100 characters")
    private String startedInTech;

    @Column(name = "current_position", length = 100)
    @Size(max = 100, message = "Current position must not exceed 100 characters")
    private String currentPosition;

    @Column(name = "work_mode", length = 20)
    @Size(max = 20, message = "Work mode must not exceed 20 characters")
    private String workMode;

    @Column(name = "salary_range", length = 50)
    @Size(max = 50, message = "Salary range must not exceed 50 characters")
    private String salaryRange;

    @ElementCollection
    @CollectionTable(name = "user_profile_movement_reasons",
            joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "reason", length = 100)
    private List<String> reasonsForMovement;

    @Column(name = "expected_salary", length = 50)
    @Size(max = 50, message = "Expected salary must not exceed 50 characters")
    private String expectedSalary;

    @Column(name = "has_completed_courses")
    private Boolean hasCompletedCourses;

    @Column(name = "projects_built")
    @Min(value = 0, message = "Projects built cannot be negative")
    private Integer projectsBuilt;

    @Column(name = "last_feedback", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Last feedback must not exceed 1000 characters")
    private String lastFeedback;

    @Column(name = "desired_position", length = 100)
    @Size(max = 100, message = "Desired position must not exceed 100 characters")
    private String desiredPosition;

    @ElementCollection
    @CollectionTable(name = "user_profile_target_jobs",
            joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "job_type", length = 100)
    private List<String> targetJobs;

    @Column(name = "daily_tasks", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Daily tasks must not exceed 1000 characters")
    private String dailyTasks;

    @Column(name = "soft_skills", columnDefinition = "TEXT")
    @Size(max = 500, message = "Soft skills must not exceed 500 characters")
    private String softSkills;

    @Column(name = "work_hours_per_week", length = 50)
    @Size(max = 50,message = "Work hours per week must not exceed 500 characters")
    private String maxHoursPerWeek;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
    }

    // --- OPTIMIZATION: Updated constructor to take ageRange instead of age ---
    public UserProfile(User user, String name, String ageRange,String currentPosition) {
        this.user = user;
        this.name = name;
        this.ageRange = ageRange; // Use ageRange
        this.currentPosition = currentPosition;
    }

    public Boolean getCaregiver() {
        return isCaregiver;
    }

    public void setCaregiver(Boolean caregiver) {
        isCaregiver = caregiver;
    }

    public String getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public void setMaxHoursPerWeek(String maxHoursPerWeek) {
        this.maxHoursPerWeek = maxHoursPerWeek;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    // --- OPTIMIZATION: Getter/Setter for ageRange ---
    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }
    // --- Removed getAge() and setAge() ---

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }
    public Boolean getIsCaregiver() { return isCaregiver; }
    public void setIsCaregiver(Boolean isCaregiver) { this.isCaregiver = isCaregiver; }
    public String getCaregivingHoursPerWeek() { return caregivingHoursPerWeek; }
    public void setCaregivingHoursPerWeek(String caregivingHoursPerWeek) { this.caregivingHoursPerWeek = caregivingHoursPerWeek; }
    public String getInitialEducation() { return initialEducation; }
    public void setInitialEducation(String initialEducation) { this.initialEducation = initialEducation; }
    public String getHigherEducationArea() { return higherEducationArea; }
    public void setHigherEducationArea(String higherEducationArea) { this.higherEducationArea = higherEducationArea; }
    public String getTechnologyLanguage() { return technologyLanguage; }
    public void setTechnologyLanguage(String technologyLanguage) { this.technologyLanguage = technologyLanguage; }
    public String getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(String yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public String getStartedInTech() { return startedInTech; }
    public void setStartedInTech(String startedInTech) { this.startedInTech = startedInTech; }
    public String getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }
    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }
    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }
    public List<String> getReasonsForMovement() { return reasonsForMovement; }
    public void setReasonsForMovement(List<String> reasonsForMovement) { this.reasonsForMovement = reasonsForMovement; }
    public String getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(String expectedSalary) { this.expectedSalary = expectedSalary; }
    public Boolean getHasCompletedCourses() { return hasCompletedCourses; }
    public void setHasCompletedCourses(Boolean hasCompletedCourses) { this.hasCompletedCourses = hasCompletedCourses; }
    public Integer getProjectsBuilt() { return projectsBuilt; }
    public void setProjectsBuilt(Integer projectsBuilt) { this.projectsBuilt = projectsBuilt; }
    public String getLastFeedback() { return lastFeedback; }
    public void setLastFeedback(String lastFeedback) { this.lastFeedback = lastFeedback; }
    public String getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(String desiredPosition) { this.desiredPosition = desiredPosition; }
    public List<String> getTargetJobs() { return targetJobs; }
    public void setTargetJobs(List<String> targetJobs) { this.targetJobs = targetJobs; }
    public String getDailyTasks() { return dailyTasks; }
    public void setDailyTasks(String dailyTasks) { this.dailyTasks = dailyTasks; }
    public String getSoftSkills() { return softSkills; }
    public void setSoftSkills(String softSkills) { this.softSkills = softSkills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", name='" + name + '\'' +
                ", ageRange='" + ageRange + '\'' + // Show ageRange
                ", currentPosition='" + currentPosition + '\'' +
                ", desiredPosition='" + desiredPosition + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}