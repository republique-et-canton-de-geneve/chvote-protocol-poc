package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;

/**
 * Model class containing the necessary information for confirmation of the vote
 */
public class Confirmation {
    private final BigInteger y_circ;
    private final NonInteractiveZKP pi;

    public Confirmation(BigInteger y_circ, NonInteractiveZKP pi) {
        this.y_circ = y_circ;
        this.pi = pi;
    }

    public BigInteger getY_circ() {
        return y_circ;
    }

    public NonInteractiveZKP getPi() {
        return pi;
    }
}
