package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Missing javadoc!
 */
public class FinalizationCodePart {
    private final byte[] F;
    private final List<BigInteger> bold_r;

    public FinalizationCodePart(byte[] f, List<BigInteger> bold_r) {
        F = f;
        this.bold_r = bold_r;
    }

    public byte[] getF() {
        return F;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }
}
