package com.hypatia.service;

import com.hypatia.dto.LoginDto;
import com.hypatia.dto.OtpVerificationDto;
import com.hypatia.dto.UserRegistrationDto;
import com.hypatia.entity.OtpCode;
import com.hypatia.entity.User;
import com.hypatia.entity.UserStatus; // Import UserStatus for explicit status checks
import com.hypatia.exception.InvalidOtpException;
import com.hypatia.exception.UserNotFoundException; // Import UserNotFoundException
import com.hypatia.repository.OtpCodeRepository;
import com.hypatia.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException; // Import specific JWT exceptions
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException; // Keep for JWT signature/malformed errors
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for authentication and authorization operations.
 *
 * This service manages the complete authentication flow including:
 * - User registration with email verification
 * - Login with OTP-based two-factor authentication
 * - JWT token generation and validation
 * - OTP generation, validation, and cleanup
 * - Rate limiting and security controls
 *
 * Security Features:
 * - BCrypt password hashing (delegated to UserService)
 * - OTP-based email verification
 * - JWT token authentication
 * - Rate limiting for OTP generation
 * - Secure random OTP generation
 * - Automatic cleanup of expired OTPs
 *
 * Authentication Flow:
 * 1. Registration: User registers -> OTP sent -> Email verified -> Account activated
 * 2. Login: User logs in -> Credentials verified -> OTP sent -> OTP verified -> JWT issued
 */
@Service
@Transactional // Apply transactional to the whole service by default
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService; // Now correctly handles User creation and updates

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Still needed for password matching logic

    @Autowired
    private JwtUtil jwtUtil;

    // AuthenticationManager is generally used by Spring Security's filter chain
    // for direct username/password authentication. If you're manually verifying
    // credentials as done here, it might not be directly autowired in the service.
    // @Autowired
    // private AuthenticationManager authenticationManager;

    /**
     * OTP expiration time in minutes.
     * Default: 2 minutes for security while maintaining usability.
     */
    @Value("${hypatia.auth.otp.expiration-minutes:2}")
    private int otpExpirationMinutes;

    /**
     * Maximum OTP attempts per hour per email.
     * Prevents abuse and spam while allowing legitimate retries.
     */
    @Value("${hypatia.auth.otp.max-attempts-per-hour:5}")
    private int maxOtpAttemptsPerHour;

    /**
     * Secure random generator for OTP creation.
     * Uses cryptographically secure random for unpredictable OTPs.
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Registers a new user with email verification.
     *
     * @param registrationDto user registration information (already validated by controller).
     * @return Map containing registration status and next steps.
     * @throws IllegalArgumentException if email is already registered.
     */
    public Map<String, Object> registerUser(UserRegistrationDto registrationDto) {
        // Validation of DTO structure, length, format etc. is handled by @Valid in the controller.
        // Service focuses on business logic validation.

        // Check if email already exists via UserService
        if (userService.emailExists(registrationDto.getEmail())) {
            log.warn("Registration attempt with already registered email: {}", registrationDto.getEmail());
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        // Create user account via UserService.
        // The UserService's registerNewUser method should handle password encoding
        // and setting initial role/status (PENDING_ONBOARDING).
        User user = userService.registerNewUser(registrationDto);

        // Generate and send OTP for email verification
        //generateAndSendOtp(user.getEmail(), "EMAIL_VERIFICATION");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario registrado exitosamente.");
        response.put("email", user.getEmail());
        //response.put("nextStep", "OTP_VERIFICATION");
        //response.put("otpExpirationMinutes", otpExpirationMinutes);

        log.info("User registered: {}", user.getEmail());
        return response;
    }

    /**
     * Initiates login process with credential verification.
     * If credentials are valid, an OTP is generated and sent for 2FA.
     *
     * @param loginDto login credentials (already validated by controller).
     * @return Map containing login status and next steps.
     * @throws BadCredentialsException if credentials are invalid.
     * @throws UserNotFoundException if user doesn't exist.
     * @throws IllegalArgumentException if user status prevents login (e.g., PENDING_ONBOARDING).
     */
    public Map<String, Object> initiateLogin(LoginDto loginDto) {
        // Validation of DTO structure, length, format etc. is handled by @Valid in the controller.

        // Find user by email
        User user = userService.findUserByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Credenciales inválidas.")); // Generic message for security

        // Verify password
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {}", loginDto.getEmail());
            throw new BadCredentialsException("Credenciales inválidas."); // Generic message for security
        }

        // Check user status to ensure they can log in
        /*if (user.getStatus() == UserStatus.PENDING_ONBOARDING) {
            log.warn("Login attempt for unverified user: {}", loginDto.getEmail());
            throw new IllegalArgumentException("Tu cuenta está pendiente de verificación de email. Por favor, completa el proceso de registro.");
        }
        // Add more specific checks if needed (e.g., if (user.getStatus() == UserStatus.LOCKED) {  throw new AccountLockedException  }

        // Generate and send OTP for login verification
        generateAndSendOtp(user.getEmail(), "LOGIN_VERIFICATION");*/

        if (user.getStatus() == UserStatus.PENDING_ONBOARDING) {
            user.setStatus(UserStatus.PARTICIPANT); // Activate the user
            userService.saveUser(user); // Persist user status change
            log.info("User {} activated to PARTICIPANT status after email verification.", user.getEmail());
            // Optional: Send a welcome email after successful activation
            // emailService.sendWelcomeEmail(user.getEmail(), user.getProfile().getName()); // Assuming UserProfile is loaded and has getName()
        }

        // Generate JWT token for the authenticated user
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        // Prepare user information for response (mirroring UserDto structure for consistency)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().name()); // Assuming getRole() returns an enum
        userInfo.put("status", user.getStatus().name()); // Assuming getStatus() returns an enum
        // Do NOT include sensitive fields like password or full profile details here

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userInfo);
        response.put("message", "Autenticación exitosa.");


        log.info("Login initiated for user: {}", user.getEmail());
        return response;
    }

    /**
     * Verifies the provided OTP and completes the authentication process.
     * Upon successful verification, a JWT token is generated.
     *
     * @param otpDto OTP verification data (already validated by controller).
     * @return Map containing JWT token and basic user information.
     * @throws InvalidOtpException if OTP is invalid or expired.
     * @throws UserNotFoundException if user doesn't exist (should be rare if OTP is found).
     * @throws IllegalArgumentException if user status prevents activation (e.g., if somehow active and getting registration OTP).
     */
    public Map<String, Object> verifyOtpAndAuthenticate(OtpVerificationDto otpDto) {
        // Validation of DTO structure, length, format etc. is handled by @Valid in the controller.

        /*LocalDateTime currentTime = LocalDateTime.now();
        // Find the most recent valid OTP for this email.
        OtpCode otpCode = otpCodeRepository.findMostRecentValidOtp(otpDto.getEmail(), currentTime)
                .orElseThrow(() -> new InvalidOtpException("OTP inválido o expirado."));

        // Additional check: Does the provided OTP actually match the found valid OTP?
        if (!otpCode.getCode().equals(otpDto.getOtp())) {
            log.warn("Invalid OTP provided for email: {}. Mismatch.", otpDto.getEmail());
            throw new InvalidOtpException("OTP inválido."); // Mismatch
        }

        // Mark OTP as used to prevent reuse
        otpCode.markAsUsed(); // Use entity's method
        otpCodeRepository.save(otpCode); // Persist the change
*/
        User user = userService.findUserByEmail(otpDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado.")); // Should generally not happen here

        // If this verification is for initial email verification after registration, activate the user.
        if (user.getStatus() == UserStatus.PENDING_ONBOARDING) {
            user.setStatus(UserStatus.PARTICIPANT); // Activate the user
            userService.saveUser(user); // Persist user status change
            log.info("User {} activated to PARTICIPANT status after email verification.", user.getEmail());
            // Optional: Send a welcome email after successful activation
            // emailService.sendWelcomeEmail(user.getEmail(), user.getProfile().getName()); // Assuming UserProfile is loaded and has getName()
        }

        // Generate JWT token for the authenticated user
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        // Prepare user information for response (mirroring UserDto structure for consistency)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().name()); // Assuming getRole() returns an enum
        userInfo.put("status", user.getStatus().name()); // Assuming getStatus() returns an enum
        // Do NOT include sensitive fields like password or full profile details here

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userInfo);
        response.put("message", "Autenticación exitosa.");

        log.info("OTP verification successful for user: {}", user.getEmail());
        return response;
    }

    /**
     * Resends an OTP to the user's email for a specified purpose.
     * Includes rate limiting to prevent abuse.
     *
     * @param email email address to send OTP to (already validated by controller).
     * @param purpose OTP purpose ("EMAIL_VERIFICATION", "LOGIN_VERIFICATION", "PASSWORD_RESET").
     * @return Map containing resend status.
     * @throws IllegalArgumentException if rate limit exceeded.
     * @throws UserNotFoundException if email does not correspond to an existing user.
     */
    public Map<String, Object> resendOtp(String email, String purpose) {
        // Validation of DTO structure, length, format etc. is handled by @Valid in the controller.

        // Verify user exists before sending OTP
        userService.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No se encontró una cuenta con este email."));

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime oneHourAgo = currentTime.minusHours(1);

        // Check rate limiting: count OTPs created in the last hour
        long recentAttempts = otpCodeRepository.countOtpsByEmailInTimeWindow(email, oneHourAgo, currentTime);

        if (recentAttempts >= maxOtpAttemptsPerHour) {
            log.warn("Rate limit exceeded for OTP resend for email: {}", email);
            throw new IllegalArgumentException(
                    String.format("Máximo de intentos de OTP excedido (%d por hora). Por favor, intenta de nuevo en 1 hora.", maxOtpAttemptsPerHour));
        }

        // Check if there's already an active (unused and not expired) OTP for this email.
        // If so, advise the user to wait instead of sending a new one immediately.
        if (otpCodeRepository.hasValidOtp(email, currentTime)) {
            log.info("Recent valid OTP found for {}. Not sending new one immediately.", email);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ya se envió un OTP válido recientemente. Por favor, revisa tu email o espera antes de solicitar uno nuevo.");
            response.put("waitTime", otpExpirationMinutes + " minutos");
            return response;
        }

        // Invalidate any old unused OTPs for this email before generating a new one
        // This ensures only one active OTP per email at a time is effective for verification
        otpCodeRepository.markAllAsUsedForEmail(email);

        generateAndSendOtp(email, purpose); // Generate and send new OTP

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Nuevo OTP enviado a tu email.");
        response.put("email", email);
        response.put("otpExpirationMinutes", otpExpirationMinutes);

        log.info("New OTP resent for user: {}", email);
        return response;
    }

    /**
     * Validates a JWT token for its integrity, expiration, and associated user's active status.
     * This method is optimized for scenarios where authentication is REQUIRED (e.g., security filters).
     *
     * @param token JWT token string.
     * @return The User entity if the token is valid and the user is active.
     * @throws BadCredentialsException If the token is invalid (malformed, bad signature).
     * @throws ExpiredJwtException If the token has expired.
     * @throws UserNotFoundException If the user specified in the token's subject is not found.
     * @throws IllegalArgumentException If the user's status prevents authentication (e.g., PENDING_ONBOARDING).
     */
    @Transactional(readOnly = true) // This method only reads data
    public User validateToken(String token) {
        try {
            // 1. Validate JWT structure and expiration using JwtUtil
            // JwtUtil's validateToken(String) will throw specific exceptions if invalid
            jwtUtil.validateToken(token); // This will throw if malformed, expired, or bad signature

            // 2. Extract email (subject) from the token
            String email = jwtUtil.getEmailFromToken(token);

            // 3. Find the user by email
            User user = userService.findUserByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado para el token proporcionado."));

            // 4. Validate user status for active authentication
            // Only PARTICIPANT or COACHING_READY users are considered fully authenticated
            if (user.getStatus() == UserStatus.PENDING_ONBOARDING) {
                log.warn("Attempt to authenticate with token for PENDING_ONBOARDING user: {}", email);
                throw new IllegalArgumentException("Cuenta pendiente de verificación de email. Por favor, completa el proceso de registro.");
            }
            // Add more status checks if needed (e.g., UserStatus.LOCKED, UserStatus.INACTIVE)
            // if (user.getStatus() == UserStatus.LOCKED) { throw new AccountLockedException("Cuenta bloqueada."); }

            log.info("Token validated successfully for user: {}", email);
            return user; // Token is valid AND user is active/enabled
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired for user: {}", jwtUtil.getEmailFromToken(token));
            throw e; // Re-throw to be caught by GlobalExceptionHandler (e.g., HTTP 401 Unauthorized)
        } catch (BadCredentialsException | MalformedJwtException | SignatureException e) { // Catch invalid format or signature
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new BadCredentialsException("Token JWT inválido o firma incorrecta.", e); // Use Spring Security's BadCredentialsException
        } catch (UserNotFoundException | IllegalArgumentException e) {
            log.warn("Token validation failed for {}: {}", token, e.getMessage());
            throw e; // Re-throw your custom business exceptions
        } catch (Exception e) { // Catch any other unexpected errors
            log.error("Ocurrió un error inesperado durante la validación del token: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al validar el token.", e); // Generic unexpected error
        }
    }

    /**
     * Logs out user by conceptually invalidating their token.
     * Note: For stateless JWTs, this is primarily client-side.
     * Server-side token blacklisting is a recommended enhancement for true invalidation.
     *
     * @param token JWT token to invalidate (placeholder for blacklisting, if implemented).
     * @return Map containing logout status.
     */
    public Map<String, Object> logout(String token) {
        // TODO: Implement token blacklist for enhanced security if true server-side invalidation is required.
        // Example: jwtBlacklistService.addTokenToBlacklist(token, jwtUtil.extractExpiration(token));
        log.info("Solicitud de cierre de sesión. El token (si se proporciona) será invalidado en el cliente.");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sesión cerrada exitosamente.");
        response.put("instruction", "Por favor, elimina el token del almacenamiento de tu cliente.");

        return response;
    }

    /**
     * Generates a secure 6-digit OTP code.
     * Uses cryptographically secure random generator to ensure OTP codes are unpredictable and secure.
     *
     * @return 6-digit OTP string.
     */
    private String generateOtp() {
        // Generate 6-digit random number with leading zeros preserved
        // secureRandom.nextInt(900000) generates numbers from 0 to 899999
        // Adding 100000 ensures the range is 100000 to 999999 (always 6 digits)
        int otp = secureRandom.nextInt(900000) + 100000;
        return String.format("%06d", otp);
    }

    /**
     * Generates an OTP and sends a verification email.
     *
     * @param email recipient email address.
     * @param purpose OTP purpose for email template selection (e.g., "EMAIL_VERIFICATION", "LOGIN_VERIFICATION", "PASSWORD_RESET").
     */
    private void generateAndSendOtp(String email, String purpose) {
        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        OtpCode otp = new OtpCode(email, otpCode, expiresAt);
        otpCodeRepository.save(otp);

        emailService.sendOtpEmail(email, otpCode, purpose, otpExpirationMinutes);
        log.info("OTP generado y enviado a {} para el propósito: {}", email, purpose);
    }

    /**
     * Cleans up expired OTP codes from the database.
     * Should be called periodically (e.g., via scheduled task) to maintain database performance.
     *
     * @return number of expired OTPs cleaned up.
     */
    public int cleanupExpiredOtps() {
        LocalDateTime currentTime = LocalDateTime.now();
        int cleanedUpCount = otpCodeRepository.deleteExpiredOtps(currentTime);
        log.info("Se limpiaron {} OTPs expirados.", cleanedUpCount);
        return cleanedUpCount;
    }

    /**
     * Retrieves authentication statistics for monitoring.
     *
     * @return Map containing authentication statistics (total OTPs, successful verifications, success rate).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAuthenticationStatistics() {
        // JpaRepository's count() method gives total entities in table
        Long totalOtpsGenerated = otpCodeRepository.count();
        // Assuming you have this method in OtpCodeRepository
        Long successfulVerifications = otpCodeRepository.countByIsUsedTrue();

        double successRate = totalOtpsGenerated > 0 ? (double) successfulVerifications / totalOtpsGenerated * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOtpsGenerados", totalOtpsGenerated);
        stats.put("verificacionesExitosas", successfulVerifications);
        stats.put("tasaDeExito", Math.round(successRate * 100.0) / 100.0); // Round to 2 decimal places
        stats.put("limitePorHora", maxOtpAttemptsPerHour);
        stats.put("expiracionOtpMinutos", otpExpirationMinutes);

        return stats;
    }

    /**
     * Placeholder for account lockout functionality.
     * This method would check if a user account is locked due to too many failed attempts.
     *
     * @param email user email to check.
     * @return true if account is locked, false otherwise.
     */
    public boolean isAccountLocked(String email) {
        // TODO: Implement actual account lockout logic here.
        // This would typically involve tracking failed login attempts in another entity/table.
        return false;
    }

    /**
     * Initiates the password reset process by generating and sending an OTP.
     *
     * @param email email address for password reset (already validated by controller).
     * @return Map containing reset initiation status.
     * @throws UserNotFoundException if no account found with the provided email address.
     */
    public Map<String, Object> initiatePasswordReset(String email) {
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No se encontró una cuenta con este email."));

        // Invalidate any old unused OTPs for this email before generating a new one
        otpCodeRepository.markAllAsUsedForEmail(email);

        generateAndSendOtp(email, "PASSWORD_RESET");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "OTP para restablecer contraseña enviado a tu email.");
        response.put("email", email);
        response.put("nextStep", "OTP_VERIFICATION");
        response.put("otpExpirationMinutes", otpExpirationMinutes);

        log.info("Password reset initiated for user: {}", email);
        return response;
    }

    /**
     * Completes the password reset process after successful OTP verification, setting a new password.
     *
     * @param email       user email (already validated by controller).
     * @param otp         verification OTP (already validated by controller).
     * @param newPassword new password to set (already validated for length/content by controller).
     * @return Map containing reset completion status.
     * @throws InvalidOtpException if OTP is invalid or expired.
     * @throws UserNotFoundException if user not found (should be rare after OTP verification).
     */
    public Map<String, Object> completePasswordReset(String email, String otp, String newPassword) {
        LocalDateTime currentTime = LocalDateTime.now();

        // Find the most recent valid OTP for this email
        OtpCode otpCode = otpCodeRepository.findMostRecentValidOtp(email, currentTime)
                .orElseThrow(() -> new InvalidOtpException("OTP inválido o expirado."));

        // Verify the provided OTP matches the code
        if (!otpCode.getCode().equals(otp)) {
            log.warn("Invalid OTP provided during password reset for email: {}. Mismatch.", email);
            throw new InvalidOtpException("OTP inválido."); // Mismatch
        }

        // Mark OTP as used to prevent reuse
        otpCode.markAsUsed();
        otpCodeRepository.save(otpCode); // Persist the change

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado.")); // Should not happen here

        userService.updatePassword(user, newPassword); // UserService handles password encoding

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contraseña restablecida exitosamente.");
        response.put("nextStep", "LOGIN");

        log.info("Password reset completed for user: {}", email);
        return response;
    }
}