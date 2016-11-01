package ch.ge.ve.protopoc.service.exception;

/**
 * This exception is thrown when an attempt is made to generate more primes than are available in a group
 */
public class NotEnoughPrimesInGroupException extends Exception {
    public NotEnoughPrimesInGroupException(String message) {
        super(message);
    }
}
