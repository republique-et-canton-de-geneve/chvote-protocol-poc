package ch.ge.ve.protopoc.service.model;

import com.google.common.collect.ImmutableList;
import org.bouncycastle.util.Arrays;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObliviousTransferResponse that = (ObliviousTransferResponse) o;
        return Objects.equals(b, that.b) &&
                java.util.Arrays.deepEquals(c, that.c) &&
                Objects.equals(d, that.d);
    }

    @Override
    public int hashCode() {
        return Objects.hash(b, c, d);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ObliviousTransferResponse{");
        sb.append("b=").append(b);
        sb.append(", c=").append(java.util.Arrays.deepToString(c));
        sb.append(", d=").append(d);
        sb.append('}');
        return sb.toString();
    }
}
