package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown if a fatal error occurs during the tallying phase
 */
public class TallyingRuntimeException extends RuntimeException {
    public TallyingRuntimeException(Exception cause) {
        super(cause);
    }
}
