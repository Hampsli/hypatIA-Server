package com.hypatia.controller;

import com.hypatia.dto.*;
import com.hypatia.entity.User;
import com.hypatia.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for authentication operations.
 *
 * This controller handles all authentication-related endpoints including:
 * - User registration with email verification
 * - Login with OTP-based two-factor authentication
 * - OTP verification and JWT token generation
 * - OTP resending and rate limiting
 * - Logout and token invalidation (client-side focused, server-side if blacklist exists)
 * - Password reset functionality
 *
 * All endpoints follow RESTful conventions and return consistent JSON responses.
 * Authentication flow requires email verification via OTP for enhanced security.
 *
 * Base path: /api/auth
 *
 * Security features:
 * - Email-based OTP verification
 * - Rate limiting on OTP generation
 * - JWT token authentication
 * - Input validation handled by JSR 303/380 annotations and @ControllerAdvice
 * - Comprehensive error handling via GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600) // Consider restricting origins in production
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Registers a new user account and initiates email verification via OTP.
     *
     * POST /api/auth/register
     *
     * @param registrationDto DTO containing user registration information (name, email, password, etc.).
     * @return ResponseEntity with registration status and next steps (e.g., OTP verification prompt).
     * Returns HTTP 201 Created on success.
     * Errors handled by GlobalExceptionHandler.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // Validation handled by @Valid. Service will throw exceptions for business rules (e.g., email already exists).
        Map<String, Object> response = authService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Initiates the user login process by validating credentials and sending an OTP.
     *
     * POST /api/auth/login
     *
     * @param loginDto DTO containing user's login credentials (email, password).
     * @return ResponseEntity with login status and OTP information (prompting for OTP verification).
     * Returns HTTP 200 OK on success.
     * Errors like invalid credentials handled by GlobalExceptionHandler.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDto loginDto) {
        // Validation handled by @Valid. Service will throw exceptions for bad credentials, user not found.
        Map<String, Object> response = authService.initiateLogin(loginDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the provided OTP code and completes the authentication process.
     * Upon successful verification, a JWT token is generated.
     *
     * POST /api/auth/verify-otp
     *
     * @param otpVerificationDto DTO containing email and the OTP code for verification.
     * @return ResponseEntity with the generated JWT token and basic user information.
     * Returns HTTP 200 OK on success.
     * Errors like invalid/expired OTP handled by GlobalExceptionHandler.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerificationDto otpVerificationDto) {
        // Validation handled by @Valid. Service will throw exceptions for invalid OTP.
        Map<String, Object> response = authService.verifyOtpAndAuthenticate(otpVerificationDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Resends an OTP to the user's email for a specified purpose (e.g., login, password reset).
     * Includes rate limiting logic within the service layer.
     *
     * POST /api/auth/resend-otp
     *
     * @param resendOtpRequestDto DTO containing the user's email and the purpose for resending the OTP.
     * @return ResponseEntity with the resend status.
     * Returns HTTP 200 OK on success.
     * Errors like email validation or rate limits handled by GlobalExceptionHandler.
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@Valid @RequestBody ResendOtpRequestDto resendOtpRequestDto) {
        // Validation handled by @Valid. Service handles rate limiting and actual resend logic.
        Map<String, Object> response = authService.resendOtp(resendOtpRequestDto.getEmail(), resendOtpRequestDto.getPurpose());
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out a user by conceptually invalidating their token.
     * For stateless JWTs, this primarily involves instructing the client to discard the token.
     * Server-side token blacklisting would be implemented in the AuthService if needed.
     *
     * POST /api/auth/logout
     *
     * @param logoutRequestDto DTO containing the token to be invalidated.
     * @return ResponseEntity with a logout success message.
     * Returns HTTP 200 OK.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody LogoutRequestDto logoutRequestDto) {
        // No @Valid here for flexibility, but DTO ensures type safety.
        // Service handles any server-side invalidation.
        Map<String, Object> response = authService.logout(logoutRequestDto.getToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates the password reset process by sending an OTP to the user's email.
     *
     * POST /api/auth/forgot-password
     *
     * @param forgotPasswordRequestDto DTO containing the email address for password reset.
     * @return ResponseEntity with the reset initiation status.
     * Returns HTTP 200 OK on success.
     * Errors like email not found handled by GlobalExceptionHandler.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto) {
        // Validation handled by @Valid. Service ensures user exists and sends OTP.
        Map<String, Object> response = authService.initiatePasswordReset(forgotPasswordRequestDto.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Completes the password reset process after OTP verification, setting a new password.
     *
     * POST /api/auth/reset-password
     *
     * @param resetPasswordRequestDto DTO containing the email, OTP, and the new password.
     * @return ResponseEntity with the reset completion status.
     * Returns HTTP 200 OK on success.
     * Errors like invalid OTP or password criteria handled by GlobalExceptionHandler.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        // All validation for email, OTP, and new password length is now handled by @Valid on the DTO.
        Map<String, Object> response = authService.completePasswordReset(
                resetPasswordRequestDto.getEmail(),
                resetPasswordRequestDto.getOtp(),
                resetPasswordRequestDto.getNewPassword()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves authentication statistics for monitoring and analytics.
     *
     * GET /api/auth/stats
     *
     * @return ResponseEntity with authentication statistics.
     * Returns HTTP 200 OK on success.
     * Errors handled by GlobalExceptionHandler.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuthStats() {
        Map<String, Object> stats = authService.getAuthenticationStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint for the authentication service.
     * Provides basic status information.
     *
     * GET /api/auth/health
     *
     * @return ResponseEntity with service health status.
     * Always returns HTTP 200 OK.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "Authentication Service",
                "timestamp", LocalDateTime.now(),
                "message", "Authentication service is operational"
        ));
    }

    /**
     * Validates a given JWT token. Primarily for internal testing or specific gateway use cases.
     * POST /api/auth/validate-token
     * @param tokenValidationRequestDto DTO containing token to validate
     * @return ResponseEntity with token validation result (valid: true/false) and basic user details if valid.
     * Returns HTTP 200 OK for valid/invalid status, HTTP 401 Unauthorized for invalid token.
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@Valid @RequestBody TokenValidationRequestDto tokenValidationRequestDto) {
        // Validation for token not blank handled by @Valid.
        // authService.validateToken now returns a User object directly or throws an exception.
        User user = authService.validateToken(tokenValidationRequestDto.getToken()); // <--- REMOVED .orElse(null)

        // If the above line executes successfully, 'user' is guaranteed to be a non-null User object.
        // No need for null checks or Optional handling here because exceptions handle failure.

        // Convert User entity to UserDto for safe public exposure
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole().name()); // Assuming role is an enum in User entity
        userDto.setStatus(user.getStatus().name()); // Assuming status is an enum in User entity

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "user", userDto // Return the safe UserDto
        ));
        // Any exceptions thrown by authService.validateToken() will be caught by GlobalExceptionHandler.
        // For example, if the token is invalid/expired, GlobalExceptionHandler will return HTTP 401/403.
    }

    /**
     * Triggers the cleanup of expired OTP codes from the database.
     * This is typically an administrative or scheduled endpoint.
     *
     * POST /api/auth/cleanup-otps
     *
     * @return ResponseEntity with the number of OTPs cleaned up.
     * Returns HTTP 200 OK on success.
     * Errors handled by GlobalExceptionHandler.
     */
    @PostMapping("/cleanup-otps")
    public ResponseEntity<Map<String, Object>> cleanupExpiredOtps() {
        int cleanedUp = authService.cleanupExpiredOtps();
        return ResponseEntity.ok(Map.of(
                "message", "Expired OTPs cleaned up successfully",
                "count", cleanedUp
        ));
    }
}