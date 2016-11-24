package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when an error occurs while the voting client is trying to confirm a vote
 */
public class VoteConfirmationException extends Exception {
    public VoteConfirmationException(Exception cause) {
        super(cause);
    }
}
