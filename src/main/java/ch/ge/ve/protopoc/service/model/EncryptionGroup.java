package ch.ge.ve.protopoc.service.model;

import com.google.common.base.Preconditions;

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
        // TODO define required certainty levels
        Preconditions.checkArgument(p.isProbablePrime(100));
        Preconditions.checkArgument(q.isProbablePrime(80));
        Preconditions.checkArgument(q.bitLength() == p.bitLength() - 1);
        Preconditions.checkArgument(g.compareTo(BigInteger.ZERO) != 0);
        Preconditions.checkArgument(h.compareTo(BigInteger.ZERO) != 0);
        this.p = p;
        this.q = q;
        this.g = g;
        this.h = h;
    }
}
