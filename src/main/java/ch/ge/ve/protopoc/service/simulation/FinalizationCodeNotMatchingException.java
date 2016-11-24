package ch.ge.ve.protopoc.service.simulation;

/**
 * Exception thrown by the voter simulator when the finalization code does not match
 */
public class FinalizationCodeNotMatchingException extends Exception {
    public FinalizationCodeNotMatchingException(String message) {
        super(message);
    }
}
