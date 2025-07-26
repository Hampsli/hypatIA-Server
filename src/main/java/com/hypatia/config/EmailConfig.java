package com.hypatia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Email configuration for OTP verification and notifications.
 * 
 * This configuration sets up email functionality for:
 * - OTP (One-Time Password) delivery during registration and login
 * - Password reset notifications
 * - Assessment completion notifications
 * - System alerts and communications
 * 
 * Email Provider Support:
 * - Gmail SMTP (default configuration)
 * - Office 365 / Outlook
 * - Custom SMTP servers
 * - Development testing with mock services
 * 
 * Security Features:
 * - TLS/SSL encryption for secure transmission
 * - STARTTLS support for enhanced security
 * - Authentication with username/password or OAuth2
 * - Connection timeout and retry configuration
 * 
 * Environment Variables Required:
 * - EMAIL_HOST: SMTP server hostname (e.g., smtp.gmail.com)
 * - EMAIL_PORT: SMTP server port (587 for TLS, 465 for SSL)
 * - EMAIL_USERNAME: Sender email address
 * - EMAIL_PASSWORD: App password or authentication token
 * 
 * Gmail Configuration:
 * 1. Enable 2-factor authentication on Gmail account
 * 2. Generate app-specific password in Google Account settings
 * 3. Use app password (not regular Gmail password)
 * 4. Set EMAIL_HOST=smtp.gmail.com, EMAIL_PORT=587
 * 
 * Required Libraries:
 * - spring-boot-starter-mail
 * - javax.mail-api (included with starter)
 * 
 * @author hypatIA Development Team
 */
@Configuration
public class EmailConfig {

    /**
     * SMTP server hostname for email delivery.
     * Default: smtp.gmail.com for Gmail integration
     * 
     * Other common SMTP servers:
     * - smtp.office365.com (Microsoft Office 365)
     * - smtp.mailgun.org (Mailgun service)
     * - smtp.sendgrid.net (SendGrid service)
     */
    @Value("${email.host:smtp.gmail.com}")
    private String emailHost;

    /**
     * SMTP server port for email connections.
     * Default: 587 for STARTTLS (recommended)
     * 
     * Common SMTP ports:
     * - 587: STARTTLS (secure, recommended)
     * - 465: SSL/TLS (secure, legacy)
     * - 25: Unencrypted (not recommended for production)
     */
    @Value("${email.port:587}")
    private int emailPort;

    /**
     * Email username for SMTP authentication.
     * This should be the full email address used for sending.
     * 
     * Security Note:
     * - Use environment variables, not hardcoded values
     * - For Gmail, use the full email address
     * - For custom domains, follow provider requirements
     */
    @Value("${email.username:}")
    private String emailUsername;

    /**
     * Email password for SMTP authentication.
     * 
     * Security Best Practices:
     * - Never commit passwords to version control
     * - Use app-specific passwords for Gmail
     * - Consider OAuth2 for enhanced security
     * - Rotate passwords regularly
     * 
     * Gmail Setup:
     * 1. Go to Google Account settings
     * 2. Security > 2-Step Verification > App passwords
     * 3. Generate new app password for "Mail"
     * 4. Use generated password, not regular Gmail password
     */
    @Value("${email.password:}")
    private String emailPassword;

    /**
     * Java Mail Sender configuration bean.
     * 
     * This bean configures the mail sender with:
     * - SMTP server connection details
     * - Authentication credentials
     * - Security protocols (TLS/SSL)
     * - Connection timeouts and retry settings
     * - Debug options for troubleshooting
     * 
     * Properties Explained:
     * 
     * Authentication:
     * - mail.smtp.auth=true: Enable SMTP authentication
     * - Username and password provided for authentication
     * 
     * Security:
     * - mail.smtp.starttls.enable=true: Enable STARTTLS encryption
     * - mail.smtp.starttls.required=true: Require STARTTLS for security
     * - mail.smtp.ssl.trust: Trust the SMTP server certificate
     * 
     * Connection:
     * - mail.smtp.connectiontimeout: Time to wait for connection (ms)
     * - mail.smtp.timeout: Time to wait for response (ms)
     * - mail.smtp.writetimeout: Time to wait for write operations (ms)
     * 
     * Debugging:
     * - mail.debug=true: Enable detailed logging (development only)
     * 
     * @return JavaMailSender configured for SMTP operations
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // Basic SMTP configuration
        mailSender.setHost(emailHost);
        mailSender.setPort(emailPort);
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);
        
        // Advanced SMTP properties for security and reliability
        Properties props = mailSender.getJavaMailProperties();
        
        // Authentication settings
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        
        // Security settings for encrypted communication
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", emailHost);
        
        // Connection timeout settings (30 seconds each)
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");
        
        // Debug settings (enable for troubleshooting)
        props.put("mail.debug", "false");
        
        // Additional properties for specific email providers
        if (emailHost.contains("gmail")) {
            // Gmail-specific optimizations
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }
        
        return mailSender;
    }

    /**
     * Email template configuration for consistent branding.
     * 
     * This method would configure:
     * - HTML templates for different email types
     * - Consistent styling and branding
     * - Internationalization support
     * - Template variables and placeholders
     * 
     * Email Templates:
     * - OTP verification email
     * - Welcome email after registration
     * - Assessment completion notification
     * - Password reset instructions
     * 
     * Implementation Note:
     * This is a placeholder for future email template configuration.
     * Consider using Thymeleaf or FreeMarker for template processing.
     * 
     * @return Email template configuration (to be implemented)
     */
    // TODO: Implement email template configuration
    // @Bean
    // public EmailTemplateConfig emailTemplateConfig() {
    //     return new EmailTemplateConfig();
    // }
}
