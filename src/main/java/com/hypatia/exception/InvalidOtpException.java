package com.hypatia.exception;

/**
 * Excepción lanzada cuando un código OTP (One-Time Password) es inválido,
 * ha expirado o ya ha sido utilizado.
 * Esta excepción es útil para diferenciar problemas de validación de OTP
 * de otros errores de autenticación.
 */
public class InvalidOtpException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de detalle para la excepción.
     *
     * @param message El mensaje de detalle (ej. "OTP inválido o expirado").
     */
    public InvalidOtpException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje de detalle y la causa de la excepción.
     *
     * @param message El mensaje de detalle.
     * @param cause La causa de la excepción.
     */
    public InvalidOtpException(String message, Throwable cause) {
        super(message, cause);
    }
}