package com.hypatia.exception;

/**
 * Excepción lanzada cuando ocurre un error en la comunicación o procesamiento
 * con el servicio externo de IA (por ejemplo, FastAPI, Hugging Face).
 * Esta excepción encapsula fallos relacionados con la disponibilidad del servicio,
 * errores de red, o problemas en la respuesta de la IA.
 */
public class AIServiceException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de detalle para la excepción.
     *
     * @param message El mensaje de detalle (ej. "Error de comunicación con el servicio de IA").
     */
    public AIServiceException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje de detalle y la causa raíz de la excepción.
     * Esto es útil para encapsular excepciones de nivel inferior (como WebClientException,
     * JsonProcessingException) que ocurren durante la interacción con la IA.
     *
     * @param message El mensaje de detalle.
     * @param cause La causa raíz de la excepción.
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}