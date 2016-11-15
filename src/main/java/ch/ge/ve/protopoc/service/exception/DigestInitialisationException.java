package ch.ge.ve.protopoc.service.exception;

/**
 * This is the exception thrown when an unexpected (and non recoverable) exception is encountered upon
 * initialising the required digests
 */
public class DigestInitialisationException extends RuntimeException {
    public DigestInitialisationException(Throwable cause) {
        super(cause);
    }
}
