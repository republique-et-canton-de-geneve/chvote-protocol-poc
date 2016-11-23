package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when an authority deems that a ballot was invalid.
 */
public class IncorrectBallotException extends Exception {
    public IncorrectBallotException(String message) {
        super(message);
    }
}
