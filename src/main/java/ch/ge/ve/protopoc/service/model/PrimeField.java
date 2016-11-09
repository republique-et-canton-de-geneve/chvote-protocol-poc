package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;

/**
 * Missing javadoc!
 */
public class PrimeField {
    private final BigInteger p_prime;

    public PrimeField(BigInteger p_prime) {
        this.p_prime = p_prime;
    }

    public BigInteger getP_prime() {
        return p_prime;
    }
}
