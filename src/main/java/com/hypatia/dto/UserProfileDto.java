package com.hypatia.dto;

import jakarta.validation.constraints.*;
import java.util.List; // Import java.util.List for collection types

/**
 * Data Transfer Object para los datos del perfil de usuario.
 *
 * Este DTO se utiliza para recibir actualizaciones del perfil desde el cliente
 * y para enviar los datos del perfil como respuesta desde la API.
 * Contiene todas las posibles piezas de información que un usuario puede proporcionar.
 */
public class UserProfileDto {

    private Long userId;

    // --- NEW / UPDATED FIELDS (from UserProfile entity) ---
    @NotBlank(message = "El nombre es un campo requerido.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    private String name; // Added: For user's full name

    @NotBlank(message = "El rango de edad es un campo requerido.")
    @Size(max = 50, message = "El rango de edad no puede exceder los 50 caracteres.")
    private String ageRange; // Added: For user's age range (as string)

    @NotBlank(message = "El rol actual es un campo requerido.")
    @Size(max = 100, message = "El rol actual no puede exceder los 100 caracteres.")
    private String currentRole; // Added: For user's current professional role
    // --- END NEW / UPDATED FIELDS ---


    @NotBlank(message = "El género es un campo requerido.")
    private String gender;

    @NotBlank(message = "El nivel de estudios es un campo requerido.")
    private String initialEducation;

    @NotBlank(message = "Los años de experiencia son un campo requerido.")
    private String yearsOfExperience;

    @NotBlank(message = "La posición deseada es un campo requerido.")
    private String desiredPosition;

    // --- Campos Opcionales ---
    private String cvPath;
    private Boolean isCaregiver;
    private String caregivingHoursPerWeek;
    private String higherEducationArea;
    private String technologyLanguage;
    private String startedInTech;
    private String currentPosition; // Duplicates 'currentRole' if 'currentRole' is current position. Consider consolidation.
    private String workMode;
    private String salaryRange;

    // --- OPTIMIZATION: Changed from String to List<String> to match UserProfile entity ---
    private List<String> reasonsForMovement; // Now a List<String>
    // --- END OPTIMIZATION ---

    private String expectedSalary;
    private Boolean hasCompletedCourses;
    private Integer projectsBuilt;
    private String lastFeedback;

    // --- OPTIMIZATION: Changed from String to List<String> to match UserProfile entity ---
    private List<String> targetJobs; // Now a List<String>
    // --- END OPTIMIZATION ---

    private String dailyTasks;
    private String softSkills;

    // Constructor por defecto
    public UserProfileDto() {}

    // --- Getters y Setters (actualizados para incluir los nuevos campos y tipos de lista) ---

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    // Getters y Setters para name, ageRange, currentRole
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }


    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getInitialEducation() { return initialEducation; }
    public void setInitialEducation(String initialEducation) { this.initialEducation = initialEducation; }

    public String getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(String yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(String desiredPosition) { this.desiredPosition = desiredPosition; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public Boolean getIsCaregiver() { return isCaregiver; }
    public void setIsCaregiver(Boolean isCaregiver) { this.isCaregiver = isCaregiver; }

    public String getCaregivingHoursPerWeek() { return caregivingHoursPerWeek; }
    public void setCaregivingHoursPerWeek(String caregivingHoursPerWeek) { this.caregivingHoursPerWeek = caregivingHoursPerWeek; }

    public String getHigherEducationArea() { return higherEducationArea; }
    public void setHigherEducationArea(String higherEducationArea) { this.higherEducationArea = higherEducationArea; }

    public String getTechnologyLanguage() { return technologyLanguage; }
    public void setTechnologyLanguage(String technologyLanguage) { this.technologyLanguage = technologyLanguage; }

    public String getStartedInTech() { return startedInTech; }
    public void setStartedInTech(String startedInTech) { this.startedInTech = startedInTech; }

    public String getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    // Getters y Setters para List<String>
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

    public List<String> getTargetJobs() { return targetJobs; }
    public void setTargetJobs(List<String> targetJobs) { this.targetJobs = targetJobs; }

    public String getDailyTasks() { return dailyTasks; }
    public void setDailyTasks(String dailyTasks) { this.dailyTasks = dailyTasks; }

    public String getSoftSkills() { return softSkills; }
    public void setSoftSkills(String softSkills) { this.softSkills = softSkills; }
}