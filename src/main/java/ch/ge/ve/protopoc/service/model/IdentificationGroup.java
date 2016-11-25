package ch.ge.ve.protopoc.service.model;

import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * The model class representing the identification group
 */
public class IdentificationGroup {
    private final BigInteger p_circ;
    private final BigInteger q_circ;
    private final BigInteger g_circ;

    public IdentificationGroup(BigInteger p_circ, BigInteger q_circ, BigInteger g_circ) {
        Preconditions.checkArgument(q_circ.bitLength() <= p_circ.bitLength());
        Preconditions.checkArgument(g_circ.compareTo(BigInteger.ONE) != 0);
        Preconditions.checkArgument(p_circ.subtract(BigInteger.ONE).mod(q_circ)
                .compareTo(BigInteger.ZERO) == 0);
        this.p_circ = p_circ;
        this.q_circ = q_circ;
        this.g_circ = g_circ;
    }

    public BigInteger getP_circ() {
        return p_circ;
    }

    public BigInteger getQ_circ() {
        return q_circ;
    }

    public BigInteger getG_circ() {
        return g_circ;
    }
}
