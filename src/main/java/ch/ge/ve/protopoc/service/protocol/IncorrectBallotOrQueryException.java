package ch.ge.ve.protopoc.service.protocol;

/**
 * Exception thrown when an incorrect ballot or an invalid query is submitted
 */
public class IncorrectBallotOrQueryException extends Exception {
    public IncorrectBallotOrQueryException(Exception cause) {
        super(cause);
    }
}
