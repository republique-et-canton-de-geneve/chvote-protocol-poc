package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when an error occurs during the vote casting process
 */
public class VoteCastingException extends Exception {
    public VoteCastingException(Exception cause) {
        super(cause);
    }
}
