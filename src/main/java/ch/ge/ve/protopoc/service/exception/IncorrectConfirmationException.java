package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when an authority receives a confirmation that it deems incorrect
 */
public class IncorrectConfirmationException extends Exception {
    public IncorrectConfirmationException(String message) {
        super(message);
    }
}
