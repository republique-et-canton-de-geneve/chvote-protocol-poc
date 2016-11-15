package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown when applying the protocol results in an error situation due to parameters being incompatible.
 */
public class IncompatibleParametersException extends Exception {
    public IncompatibleParametersException(String s) {
        super(s);
    }
}
