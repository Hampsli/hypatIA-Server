package com.hypatia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * OtpCode entity for email-based authentication verification.
 * 
 * This entity manages One-Time Passwords (OTP) for secure authentication:
 * - Registration verification to confirm email ownership
 * - Login verification for enhanced security
 * - Password reset authentication
 * - Account recovery and security operations
 * 
 * Security Features:
 * - Time-limited codes (2-minute expiration by default)
 * - Single-use verification (marked as used after verification)
 * - Secure random 6-digit code generation
 * - Email-based delivery for account verification
 * 
 * Authentication Flow:
 * 1. User attempts to register or login
 * 2. System generates OTP and sends via email
 * 3. User enters OTP in application
 * 4. System verifies code validity and expiration
 * 5. Code marked as used and JWT token issued
 * 
 * Database Design:
 * - Indexed by email for fast lookup during verification
 * - Automatic cleanup of expired codes recommended
 * - Audit trail with creation timestamps
 * - Support for multiple active codes per email
 * 
 * Security Considerations:
 * - Short expiration time (2 minutes) for security
 * - Rate limiting to prevent abuse and spam
 * - Secure random number generation
 * - Email delivery confirmation and retry logic
 * - Cleanup of expired and used codes
 * 
 * @author hypatIA Development Team
 */
@Entity
@Table(name = "otp_codes", schema = "public", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_code", columnList = "code"),
    @Index(name = "idx_otp_expires_at", columnList = "expires_at")
})
public class OtpCode {

    /**
     * Primary key for OTP code identification.
     * Auto-generated for unique OTP tracking.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Email address associated with the OTP code.
     * 
     * Purpose:
     * - Links OTP to specific user account
     * - Enables verification during login/registration
     * - Supports password reset and account recovery
     * - Indexed for fast lookup during verification
     * 
     * Validation:
     * - Must be valid email format
     * - Required field for all OTP operations
     * - Used for email delivery and verification
     * 
     * Security Notes:
     * - Multiple active OTPs per email may exist
     * - Email verification confirms account ownership
     * - Used for account linking and authentication
     */
    @Column(name = "email", nullable = false, length = 255)
    @NotBlank(message = "Email is required for OTP")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * The OTP code sent to the user.
     * 
     * Code Characteristics:
     * - 6-digit numeric code for user-friendly entry
     * - Cryptographically secure random generation
     * - Easy to type and remember for short duration
     * - Consistent format across all communications
     * 
     * Generation Strategy:
     * - SecureRandom for cryptographic security
     * - Range: 100000 to 999999 (6 digits)
     * - No sequential or predictable patterns
     * - Immediate delivery via email service
     * 
     * Security Features:
     * - Single-use only (marked as used after verification)
     * - Short validity period (2 minutes)
     * - Rate limiting to prevent brute force attacks
     * - Secure storage and transmission
     */
    @Column(name = "code", nullable = false, length = 6)
    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "\\d{6}", message = "OTP code must be exactly 6 digits")
    private String code;

    /**
     * Expiration timestamp for the OTP code.
     * 
     * Expiration Strategy:
     * - Default 2-minute validity period
     * - Balances security with user experience
     * - Automatic cleanup of expired codes
     * - Time zone handling with LocalDateTime
     * 
     * Security Benefits:
     * - Limits exposure window for intercepted codes
     * - Prevents replay attacks with old codes
     * - Reduces attack surface for brute force attempts
     * - Encourages immediate use and verification
     * 
     * Implementation:
     * - Set during OTP creation (now + 2 minutes)
     * - Checked during verification process
     * - Indexed for efficient expired code cleanup
     * - Used in database cleanup scheduled tasks
     */
    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "OTP expiration time is required")
    @Future(message = "OTP expiration must be in the future")
    private LocalDateTime expiresAt;

    /**
     * Flag indicating if the OTP has been used for verification.
     * 
     * Usage Tracking:
     * - Prevents code reuse for security
     * - Tracks successful verification events
     * - Enables audit trails for authentication
     * - Supports rate limiting and abuse detection
     * 
     * Workflow:
     * - Initially set to false when OTP is created
     * - Changed to true after successful verification
     * - Used codes cannot be verified again
     * - Supports cleanup of processed codes
     * 
     * Security Implementation:
     * - Single-use verification prevents replay attacks
     * - Clear audit trail of verification events
     * - Enables detection of multiple verification attempts
     * - Supports investigation of security incidents
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    /**
     * Timestamp when the OTP was created.
     * 
     * Audit Information:
     * - Tracks OTP generation time for analytics
     * - Enables performance monitoring of verification flow
     * - Supports investigation of authentication issues
     * - Used for cleanup and retention policies
     * 
     * Analytics Usage:
     * - Verification success rates over time
     * - Peak usage periods for system scaling
     * - User behavior patterns and flow optimization
     * - Security incident investigation and forensics
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor for JPA entity creation.
     */
    public OtpCode() {}

    /**
     * Constructor for creating new OTP codes.
     * 
     * @param email Email address for OTP delivery
     * @param code 6-digit OTP code
     * @param expiresAt Expiration timestamp
     */
    public OtpCode(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.isUsed = false;
    }

    /**
     * Checks if the OTP code is valid (not expired and not used).
     * 
     * Validation Logic:
     * - Code must not be marked as used
     * - Current time must be before expiration time
     * - Provides single method for complete validation
     * 
     * @return true if OTP is valid and can be used
     */
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Checks if the OTP code has expired.
     * 
     * @return true if current time is after expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Marks the OTP code as used after successful verification.
     * 
     * Security Operation:
     * - Prevents code reuse for additional verifications
     * - Creates audit trail of verification events
     * - Should be called immediately after successful verification
     */
    public void markAsUsed() {
        this.isUsed = true;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * String representation for debugging and logging.
     * Note: Excludes actual OTP code for security.
     */
    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", code='[HIDDEN]'" +
                ", expiresAt=" + expiresAt +
                ", isUsed=" + isUsed +
                ", createdAt=" + createdAt +
                '}';
    }
}
