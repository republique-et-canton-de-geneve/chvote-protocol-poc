package ch.ge.ve.protopoc.service.model;

import com.google.common.collect.ImmutableList;
import org.bouncycastle.util.Arrays;

import java.math.BigInteger;
import java.util.List;

/**
 * Model class for an Oblivious Transfer response
 */
public class ObliviousTransferResponse {
    private final List<BigInteger> b;
    private final byte[][] c;
    private final List<BigInteger> d;

    public ObliviousTransferResponse(List<BigInteger> b, byte[][] c, List<BigInteger> d) {
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public List<BigInteger> getB() {
        return ImmutableList.copyOf(b);
    }

    public byte[][] getC() {
        return Arrays.clone(c);
    }

    public List<BigInteger> getD() {
        return ImmutableList.copyOf(d);
    }
}
