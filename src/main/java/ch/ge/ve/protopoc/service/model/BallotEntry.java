package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Missing javadoc!
 */
public class BallotEntry {
    private final Integer i;
    private final BallotAndQuery alpha;
    private final List<BigInteger> bold_r;

    public BallotEntry(Integer i, BallotAndQuery alpha, List<BigInteger> bold_r) {
        this.i = i;
        this.alpha = alpha;
        this.bold_r = bold_r;
    }

    public Integer getI() {
        return i;
    }

    public BallotAndQuery getAlpha() {
        return alpha;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BallotEntry that = (BallotEntry) o;
        return Objects.equals(i, that.i) &&
                Objects.equals(alpha, that.alpha) &&
                Objects.equals(bold_r, that.bold_r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, alpha, bold_r);
    }
}
