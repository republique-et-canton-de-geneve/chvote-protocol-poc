package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown by an authority if it detects an invalid shuffle proof
 */
public class InvalidShuffleProofException extends RuntimeException {
    public InvalidShuffleProofException(String message) {
        super(message);
    }
}
