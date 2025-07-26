package com.hypatia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for handling "resource not found" scenarios (404).
 *
 * The @ResponseStatus(HttpStatus.NOT_FOUND) annotation tells Spring Boot
 * to automatically return a 404 HTTP status code whenever this exception
 * is thrown and not caught by a more specific handler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    /**
     * Constructor that accepts a message.
     * @param message The detail message explaining the reason for the exception.
     */
    public NotFoundException(String message) {
        super(message);
    }
}