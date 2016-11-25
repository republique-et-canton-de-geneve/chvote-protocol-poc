package ch.ge.ve.protopoc.service.simulation;

/**
 * Exception thrown when an error occurs during the vote simulation process
 */
public class VoteProcessException extends RuntimeException {
    public VoteProcessException(Throwable cause) {
        super(cause);
    }
}
