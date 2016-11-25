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
    private final BigInteger p;

    /**
     * Prime order q
     */
    private final BigInteger q;

    /**
     * Generator, independent from h
     */
    private final BigInteger g;

    /**
     * Generator, independent from g
     */
    private final BigInteger h;

    public EncryptionGroup(BigInteger p, BigInteger q, BigInteger g, BigInteger h) {
        // TODO define required certainty levels
        Preconditions.checkArgument(p.isProbablePrime(100));
        Preconditions.checkArgument(q.isProbablePrime(80));
        Preconditions.checkArgument(q.bitLength() == p.bitLength() - 1);
        Preconditions.checkArgument(g.compareTo(BigInteger.ONE) > 0);
        Preconditions.checkArgument(h.compareTo(BigInteger.ONE) > 0);
        this.p = p;
        this.q = q;
        this.g = g;
        this.h = h;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getH() {
        return h;
    }

    @Override
    public String toString() {
        return "EncryptionGroup{" +
                "p=" + p +
                ", q=" + q +
                ", g=" + g +
                ", h=" + h +
                '}';
    }
}
