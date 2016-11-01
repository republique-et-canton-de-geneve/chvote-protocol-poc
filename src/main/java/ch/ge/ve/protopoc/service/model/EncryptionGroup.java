package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;

/**
 * Missing javadoc!
 */
public class EncryptionGroup {
    /**
     * Safe prime modulus p
     */
    public final BigInteger p;

    /**
     * Prime order q
     */
    public final BigInteger q;

    /**
     * Generator, independent from h
     */
    public final BigInteger g;

    /**
     * Generator, independent from g
     */
    public final BigInteger h;

    public EncryptionGroup(BigInteger p, BigInteger q, BigInteger g, BigInteger h) {
        this.p = p;
        this.q = q;
        this.g = g;
        this.h = h;
    }
}
