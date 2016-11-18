package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing the tuple &lt;beta, bold_r&gt;, returned by Algorithm 5.28
 */
public class ObliviousTransferResponseAndRand {
    private final ObliviousTransferResponse beta;
    private final List<BigInteger> bold_r;

    public ObliviousTransferResponseAndRand(ObliviousTransferResponse beta, List<BigInteger> bold_r) {
        this.beta = beta;
        this.bold_r = bold_r;
    }

    public ObliviousTransferResponse getBeta() {
        return beta;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObliviousTransferResponseAndRand that = (ObliviousTransferResponseAndRand) o;
        return Objects.equals(beta, that.beta) &&
                Objects.equals(bold_r, that.bold_r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beta, bold_r);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ObliviousTransferResponseAndRand{");
        sb.append("beta=").append(beta);
        sb.append(", bold_r=").append(bold_r);
        sb.append('}');
        return sb.toString();
    }
}
