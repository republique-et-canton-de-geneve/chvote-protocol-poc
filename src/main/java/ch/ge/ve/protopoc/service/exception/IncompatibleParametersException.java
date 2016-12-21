package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when applying the protocol results in an error situation due to parameters being incompatible.
 */
public class IncompatibleParametersException extends RuntimeException {
    public IncompatibleParametersException(String s) {
        super(s);
    }

    public IncompatibleParametersException(Throwable cause) {
        super(cause);
    }
}
