package com.hypatia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException; // Import specific MailException
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime; // Explicitly import LocalDateTime for healthCheck

/**
 * Service class for email operations.
 *
 * This service handles all email communications for the hypatIA platform:
 * - OTP verification emails for registration and login
 * - Password reset emails
 * - Welcome and onboarding emails
 * - Assessment completion notifications
 * - AI-generated report delivery
 *
 * Key Features:
 * - Asynchronous email sending for better performance.
 * - HTML email templates using Thymeleaf.
 * - Proper error handling and robust logging.
 * - Support for different email types and purposes.
 * - Internationalization ready (Spanish/English - via Thymeleaf/MessageSource integration not shown here).
 * - Professional email branding.
 *
 * Security Considerations:
 * - Sensitive information (OTP codes) handled securely.
 * - Email content sanitization.
 * - Rate limiting support through service layer.
 * - Proper exception handling to prevent information leakage.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class); // Initialize logger

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Sender email address configured in application properties.
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sender name for branded emails.
     */
    @Value("${hypatia.email.from-name:hypatIA Platform}")
    private String fromName;

    /**
     * Email subject prefix for consistent branding.
     */
    @Value("${hypatia.email.subject-prefix:[hypatIA] }")
    private String subjectPrefix;

    /**
     * Base URL for email links and branding.
     */
    @Value("${hypatia.app.base-url:http://localhost:3000}")
    private String baseUrl;

    /**
     * Sends OTP verification email.
     *
     * This is the primary email type sent by the authentication system.
     * Supports different purposes with appropriate messaging and templates.
     *
     * @param email recipient email address.
     * @param otpCode 6-digit OTP code.
     * @param purpose email purpose ("EMAIL_VERIFICATION", "LOGIN_VERIFICATION", "PASSWORD_RESET").
     * @param expirationMinutes OTP expiration time in minutes.
     */
    @Async
    public void sendOtpEmail(String email, String otpCode, String purpose, int expirationMinutes) {
        try {
            // Prepare template variables
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("otpCode", otpCode);
            templateVars.put("expirationMinutes", expirationMinutes);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("purpose", purpose); // Useful for conditional rendering in template

            // Determine subject and template based on purpose
            String subject;
            String template;

            // Using switch expression for cleaner code (Java 14+) or regular switch
            switch (purpose) {
                case "EMAIL_VERIFICATION":
                    subject = "Verifica tu cuenta - Código de verificación";
                    template = "email-verification-otp";
                    templateVars.put("title", "Verifica tu cuenta");
                    templateVars.put("message", "Gracias por registrarte en hypatIA. Usa este código para verificar tu cuenta:");
                    templateVars.put("instruction", "Ingresa este código en la página de verificación para activar tu cuenta.");
                    break;

                case "LOGIN_VERIFICATION":
                    subject = "Código de acceso - Verificación de inicio de sesión";
                    template = "login-verification-otp";
                    templateVars.put("title", "Código de acceso");
                    templateVars.put("message", "Usa este código para completar tu inicio de sesión:");
                    templateVars.put("instruction", "Ingresa este código para acceder a tu cuenta de hypatIA.");
                    break;

                case "PASSWORD_RESET":
                    subject = "Restablecer contraseña - Código de verificación";
                    template = "password-reset-otp";
                    templateVars.put("title", "Restablecer contraseña");
                    templateVars.put("message", "Usa este código para restablecer tu contraseña:");
                    templateVars.put("instruction", "Si no solicitaste este cambio, ignora este email. Tu cuenta permanece segura.");
                    break;

                default:
                    subject = "Código de verificación";
                    template = "generic-otp";
                    templateVars.put("title", "Código de verificación");
                    templateVars.put("message", "Tu código de verificación es:");
                    templateVars.put("instruction", "Usa este código para completar la verificación.");
            }

            // Send email
            sendHtmlEmail(email, subject, template, templateVars);

        } catch (MailException | MessagingException e) { // Catch specific email-related exceptions
            log.error("Fallo al enviar el email de OTP a {}: {}", email, e.getMessage(), e); // Log with stack trace
        } catch (Exception e) { // Catch any other unexpected exceptions
            log.error("Ocurrió un error inesperado al enviar el email de OTP a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends welcome email after successful registration and verification.
     *
     * @param email recipient email address.
     * @param userName user's name for personalization.
     */
    @Async
    public void sendWelcomeEmail(String email, String userName) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("userName", userName);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("dashboardUrl", baseUrl + "/dashboard");
            templateVars.put("profileUrl", baseUrl + "/profile");
            templateVars.put("assessmentUrl", baseUrl + "/assessment");

            String subject = "¡Bienvenida a hypatIA! - Tu plataforma de desarrollo profesional";

            sendHtmlEmail(email, subject, "welcome", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de bienvenida a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de bienvenida a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends assessment completion notification email.
     *
     * @param email recipient email address.
     * @param userName user's name.
     * @param completionPercentage assessment completion percentage.
     */
    @Async
    public void sendAssessmentCompletionEmail(String email, String userName, Double completionPercentage) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("userName", userName);
            templateVars.put("completionPercentage", completionPercentage);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("dashboardUrl", baseUrl + "/dashboard");
            templateVars.put("reportUrl", baseUrl + "/reports");

            String subject = "Evaluación completada - Descubre tus resultados";

            sendHtmlEmail(email, subject, "assessment-completion", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de completitud de evaluación a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de completitud de evaluación a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends AI-generated report via email.
     *
     * @param email recipient email address.
     * @param userName user's name.
     * @param reportType type of report ("SKILLS_ANALYSIS", "CAREER_RECOMMENDATIONS", etc.).
     * @param reportContent AI-generated report content.
     */
    @Async
    public void sendAIReportEmail(String email, String userName, String reportType, String reportContent) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("userName", userName);
            templateVars.put("reportType", reportType);
            templateVars.put("reportContent", reportContent);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("dashboardUrl", baseUrl + "/dashboard");

            String subject = getReportSubject(reportType);

            sendHtmlEmail(email, subject, "ai-report", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de reporte AI a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de reporte AI a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends password reset confirmation email.
     *
     * @param email recipient email address.
     * @param userName user's name.
     */
    @Async
    public void sendPasswordResetConfirmationEmail(String email, String userName) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("userName", userName);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("loginUrl", baseUrl + "/login");
            templateVars.put("supportUrl", baseUrl + "/support");

            String subject = "Contraseña actualizada correctamente";

            sendHtmlEmail(email, subject, "password-reset-confirmation", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de confirmación de restablecimiento de contraseña a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de confirmación de restablecimiento de contraseña a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends onboarding follow-up email for users who haven't completed their profile.
     *
     * @param email recipient email address.
     * @param userName user's name.
     * @param daysRegistered number of days since registration.
     */
    @Async
    public void sendOnboardingFollowUpEmail(String email, String userName, int daysRegistered) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("userName", userName);
            templateVars.put("daysRegistered", daysRegistered);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("profileUrl", baseUrl + "/profile");
            templateVars.put("assessmentUrl", baseUrl + "/assessment");
            templateVars.put("supportUrl", baseUrl + "/support");

            String subject = "Completa tu perfil en hypatIA - Tu carrera te espera";

            sendHtmlEmail(email, subject, "onboarding-followup", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de seguimiento de onboarding a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de seguimiento de onboarding a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends notification about new AI features or updates.
     *
     * @param email recipient email address.
     * @param userName user's name.
     * @param featureName name of the new feature.
     * @param featureDescription description of the new feature.
     */
    @Async
    public void sendFeatureAnnouncementEmail(String email, String userName, String featureName, String featureDescription) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("userName", userName);
            templateVars.put("featureName", featureName);
            templateVars.put("featureDescription", featureDescription);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("dashboardUrl", baseUrl + "/dashboard");
            templateVars.put("featuresUrl", baseUrl + "/features");

            String subject = "Nueva funcionalidad disponible - " + featureName;

            sendHtmlEmail(email, subject, "feature-announcement", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de anuncio de característica a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de anuncio de característica a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends monthly progress report email.
     *
     * @param email recipient email address.
     * @param userName user's name.
     * @param progressData monthly progress statistics.
     */
    @Async
    public void sendMonthlyProgressEmail(String email, String userName, Map<String, Object> progressData) {
        try {
            Map<String, Object> templateVars = new HashMap<>(progressData);
            templateVars.put("userName", userName);
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("dashboardUrl", baseUrl + "/dashboard");
            templateVars.put("goalUrl", baseUrl + "/goals");

            String subject = "Tu progreso mensual en hypatIA";

            sendHtmlEmail(email, subject, "monthly-progress", templateVars);

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de progreso mensual a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de progreso mensual a {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Sends HTML email using Thymeleaf template.
     * This is the core email sending method that processes templates and sends formatted emails.
     *
     * @param to recipient email address.
     * @param subject email subject (prefix will be added).
     * @param templateName Thymeleaf template name.
     * @param templateVars variables for template processing.
     * @throws MessagingException if email sending fails.
     * @throws MailException if sending fails due to Spring Mail issues.
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateVars)
            throws MessagingException, MailException { // No longer declares UnsupportedEncodingException
        try {
            // Create MIME message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true for multipart (HTML content)

            // Set email headers
            helper.setTo(to);
            helper.setSubject(subjectPrefix + subject);
            helper.setFrom(fromEmail, fromName); // This line can throw UnsupportedEncodingException

            // Process template
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateVars);

            String htmlContent = templateEngine.process("email/" + templateName, thymeleafContext);
            helper.setText(htmlContent, true); // true indicates HTML content

            // Send email
            mailSender.send(message);

            log.info("Email enviado exitosamente a: {} con asunto: {}", to, subject);

        } catch (UnsupportedEncodingException e) {
            // Wrap the checked UnsupportedEncodingException into an unchecked MailPreparationException
            throw new MailPreparationException("Failed to encode email sender name or content due to unsupported encoding.", e);
        }
    }

    /**
     * Generates an appropriate subject line based on the report type.
     *
     * @param reportType AI report type.
     * @return localized subject line.
     */
    private String getReportSubject(String reportType) {
        switch (reportType) {
            case "SKILLS_ANALYSIS":
                return "Tu análisis de habilidades está listo";
            case "CAREER_RECOMMENDATIONS":
                return "Recomendaciones personalizadas para tu carrera";
            case "LEARNING_PATH":
                return "Tu ruta de aprendizaje personalizada";
            case "INDUSTRY_INSIGHTS":
                return "Insights de la industria para tu perfil";
            case "MENTORSHIP_MATCHES":
                return "Conexiones de mentoría recomendadas";
            default:
                return "Tu reporte personalizado está listo";
        }
    }

    /**
     * Validates an email address format using a basic regex.
     * Note: For comprehensive validation, consider using Spring's EmailValidator or a dedicated library.
     *
     * @param email email address to validate.
     * @return true if email format is valid, false otherwise.
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // This regex is basic; jakarta.validation.constraints.Email annotation is more robust
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Sends a test email for email configuration verification.
     *
     * @param to recipient email address.
     * @return true if test email was sent successfully, false otherwise.
     */
    public boolean sendTestEmail(String to) {
        try {
            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("baseUrl", baseUrl);
            templateVars.put("testTime", LocalDateTime.now().toString()); // Use LocalDateTime

            sendHtmlEmail(to, "Prueba de configuración de email", "test-email", templateVars);
            return true;

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de prueba a {}: {}", to, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de prueba a {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sends a plain text email (fallback method).
     * Used when HTML email is not required or for simple notifications.
     *
     * @param to recipient email address.
     * @param subject email subject.
     * @param content plain text content.
     */
    @Async
    public void sendPlainTextEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8"); // false for plain text

            helper.setTo(to);
            helper.setSubject(subjectPrefix + subject);
            helper.setFrom(fromEmail, fromName);
            helper.setText(content, false); // false indicates plain text

            mailSender.send(message);

            log.info("Email de texto plano enviado exitosamente a: {}", to); // Use logger

        } catch (MailException | MessagingException e) {
            log.error("Fallo al enviar el email de texto plano a {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al enviar el email de texto plano a {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Gets email service status for monitoring.
     *
     * @return Map containing email service configuration and status.
     */
    public Map<String, Object> getEmailServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("fromEmail", fromEmail);
        status.put("fromName", fromName);
        status.put("subjectPrefix", subjectPrefix);
        status.put("baseUrl", baseUrl);
        status.put("templateEngineStatus", templateEngine != null ? "Available" : "Not configured"); // Renamed key for clarity
        status.put("mailSenderStatus", mailSender != null ? "Available" : "Not configured"); // Renamed key for clarity
        status.put("asyncEnabled", true); // Assuming @EnableAsync is configured in main app

        return status;
    }
}