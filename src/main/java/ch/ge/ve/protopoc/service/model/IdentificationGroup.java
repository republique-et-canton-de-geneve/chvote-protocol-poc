package ch.ge.ve.protopoc.service.model;

import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * Missing javadoc!
 */
public class IdentificationGroup {
    public final BigInteger p_circ;
    public final BigInteger q_circ;
    public final BigInteger g_circ;

    public IdentificationGroup(BigInteger p_circ, BigInteger q_circ, BigInteger g_circ) {
        Preconditions.checkArgument(q_circ.bitLength() <= p_circ.bitLength());
        Preconditions.checkArgument(g_circ.compareTo(BigInteger.ONE) != 0);
        this.p_circ = p_circ;
        this.q_circ = q_circ;
        this.g_circ = g_circ;
    }
}
