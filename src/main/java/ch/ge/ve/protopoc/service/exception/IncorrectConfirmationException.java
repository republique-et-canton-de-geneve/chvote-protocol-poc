package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when an authority receives a confirmation that it deems incorrect
 */
public class IncorrectConfirmationException extends RuntimeException {
    public IncorrectConfirmationException(String message) {
        super(message);
    }
}
