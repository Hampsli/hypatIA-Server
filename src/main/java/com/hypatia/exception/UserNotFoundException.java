package com.hypatia.exception;

/**
 * Excepción lanzada cuando no se encuentra un usuario en el sistema.
 * Esto puede ocurrir durante el inicio de sesión, el restablecimiento de contraseña,
 * o cualquier operación que requiera que un usuario exista previamente.
 * A menudo se utiliza para mantener la seguridad, proporcionando mensajes genéricos
 * para evitar la enumeración de usuarios.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de detalle para la excepción.
     *
     * @param message El mensaje de detalle (ej. "Usuario no encontrado" o "Credenciales inválidas").
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje de detalle y la causa de la excepción.
     *
     * @param message El mensaje de detalle.
     * @param cause La causa de la excepción.
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}