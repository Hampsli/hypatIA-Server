package com.hypatia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // Import for specific exception
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException; // Import for specific exception
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; // Import Decoders for robust secret handling
import io.jsonwebtoken.security.Keys; // Import Keys for key generation
import io.jsonwebtoken.security.SignatureException; // Import for specific exception (if using io.jsonwebtoken.security.SignatureException)
// Note: Some older versions might use io.jsonwebtoken.SignatureException (deprecated). Use io.jsonwebtoken.security.SignatureException if available.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException; // Used for generic invalid token issues
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JSON Web Token (JWT) operations.
 * Handles generation, validation, and extraction of information from JWTs.
 * Supports token creation for Spring Security's UserDetails and custom user data.
 */
@Component
public class JwtUtil {

    // The secret key used for signing and verifying JWTs.
    // Must be long and complex enough for security. Base64 encoded recommended.
    @Value("${jwt.secret}")
    private String secret;

    // The expiration time for JWTs in milliseconds.
    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    /**
     * Retrieves the signing key for JWTs.
     * Decodes the secret string from Base64 to ensure proper key generation.
     *
     * @return The signing Key.
     */
    private Key getSigningKey() {
        try {
            // Decoders.BASE64.decode() itself does not throw UnsupportedEncodingException.
            // If you were using `secret.getBytes("UTF-8")` or similar, that's where it would be thrown.
            // However, wrapping it in a try-catch is a good defensive programming measure
            // for any potential low-level encoding or decoding issues from external libraries.
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) { // Catching a generic Exception here to wrap potential decoding issues
            // This indicates a fundamental problem with your JWT secret configuration.
            // For example, if the secret is not a valid Base64 string.
            throw new RuntimeException("Failed to decode JWT secret or generate signing key. Check 'jwt.secret' configuration.", e);
        }
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token The JWT token.
     * @return The username.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the email from a JWT token.
     * This is an alias for extractUsername, as email is typically the subject.
     * @param token The JWT token.
     * @return The email address (subject).
     */
    public String getEmailFromToken(String token) {
        return extractUsername(token); // Reuses the existing extractUsername logic
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token.
     * @return The expiration Date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a Claims resolver function.
     *
     * @param token        The JWT token.
     * @param claimsResolver A function to resolve the desired claim from the Claims.
     * @param <T>          The type of the claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     * This method handles parsing and signature verification.
     *
     * @param token The JWT token.
     * @return All Claims from the token.
     * @throws ExpiredJwtException       if the token is expired.
     * @throws MalformedJwtException     if the token is not a valid JWT.
     * @throws SignatureException        if the token's signature is invalid.
     * @throws IllegalArgumentException  if the token is null or empty.
     */
    private Claims extractAllClaims(String token) {
        // This is where JWT parsing and basic validation (signature, malformation) happen.
        // Exceptions from here will be caught and re-thrown by public validateToken(String)
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token The JWT token.
     * @return True if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a JWT token for Spring Security's `UserDetails` interface.
     * This method is typically used by authentication providers.
     *
     * @param userDetails The `UserDetails` object containing user information.
     * @return The generated JWT token.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Optional: If your UserDetails implementation (`CustomUserDetails`)
        // holds additional data like userId, you can add it to claims here:
        // if (userDetails instanceof CustomUserDetails) {
        //     claims.put("userId", ((CustomUserDetails) userDetails).getUserId());
        // }
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generates a JWT token using an email as the subject and includes a userId as a custom claim.
     * This is the primary method for generating tokens for authenticated users in AuthService.
     *
     * @param email  The user's email (will be the token subject).
     * @param userId The user's ID (will be added as a custom claim "userId").
     * @return The generated JWT token.
     */
    public String generateToken(String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) {
            claims.put("userId", userId); // Add userId to claims
        }
        return createToken(claims, email); // Use email as the subject
    }

    /**
     * Creates the JWT token with specified claims, subject, issued at, expiration, and signature.
     *
     * @param claims  Custom claims to include in the token.
     * @param subject The subject of the token (typically username/email).
     * @return The compact JWT string.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token against a `UserDetails` object.
     * Used typically by Spring Security filters to validate a token against a loaded user.
     *
     * @param token       The JWT token.
     * @param userDetails The `UserDetails` object to validate against.
     * @return True if the token is valid for the given user, false otherwise.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // This extract will implicitly validate structure/signature/expiration
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        // Note: The extractUsername call (via extractAllClaims) will throw exceptions
        // if the token is invalid or expired. So, the !isTokenExpired(token) check here
        // might be redundant if extractUsername already handles expiration via Jwts.parserBuilder.
        // For robustness, catching specific exceptions (as in the overloaded validateToken(String)) is better.
    }

    /**
     * Validates a JWT token for its integrity and expiration.
     * This method explicitly throws specific exceptions for different validation failures,
     * making it suitable for use in service layers (e.g., AuthService) where detailed
     * error handling is required.
     *
     * @param token The JWT token string.
     * @return True if the token is structurally valid, has a valid signature, and is not expired.
     * @throws IllegalArgumentException  if the token is null or empty.
     * @throws MalformedJwtException     if the token is not a valid JWT.
     * @throws SignatureException        if the token's signature is invalid.
     * @throws ExpiredJwtException       if the token has expired.
     * @throws RuntimeException          for any other unexpected parsing/validation errors.
     */
    public Boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("JWT token cannot be null or empty.");
        }
        try {
            // Attempt to parse claims. This method inherently verifies signature and expiration.
            // If validation fails, it throws specific JWT exceptions.
            extractAllClaims(token);
            return true; // If no exception is thrown, the token is valid.
        } catch (ExpiredJwtException e) {
            // Log this specific exception if desired
            throw e; // Re-throw to be handled by calling service/controller advice
        } catch (MalformedJwtException | SignatureException e) { // Catch invalid format or signature
            throw new BadCredentialsException("Invalid JWT token or signature: " + e.getMessage(), e); // Use Spring Security's BadCredentialsException for consistency
        } catch (Exception e) {
            // Catch any other unexpected exceptions from the JWT library
            throw new RuntimeException("Unexpected error during JWT validation: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the userId from a JWT token's claims.
     * Requires the userId to have been added as a custom claim ("userId") during token creation.
     *
     * @param token The JWT token.
     * @return The userId as a Long, or null if the claim is not present or not a Long.
     */
    public Long getUserIdFromToken(String token) {
        // Attempt to extract the "userId" claim as a Long
        // If "userId" is not present or not convertible to Long, it will return null.
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
}