package ch.ge.ve.protopoc.service.simulation;

/**
 * Exception thrown by the voter simulator when the return codes do not match
 */
public class ReturnCodesNotMatchingException extends Exception {
    public ReturnCodesNotMatchingException(String message) {
        super(message);
    }
}
