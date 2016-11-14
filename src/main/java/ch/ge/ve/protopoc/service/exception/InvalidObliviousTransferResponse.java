package ch.ge.ve.protopoc.service.exception;

/**
 * Exception thrown on the voting client when an invalid oblivious transfer response is detected.
 * (or a computation error occurred...)
 */
public class InvalidObliviousTransferResponse extends Exception {
    public InvalidObliviousTransferResponse(String s) {
        super(s);
    }
}
