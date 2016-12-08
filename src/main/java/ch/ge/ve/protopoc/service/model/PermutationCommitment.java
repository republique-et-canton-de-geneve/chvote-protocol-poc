package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing a permutation commitment
 */
public class PermutationCommitment {
    private final List<BigInteger> bold_c;
    private final List<BigInteger> bold_r;

    public PermutationCommitment(List<BigInteger> bold_c, List<BigInteger> bold_r) {
        this.bold_c = bold_c;
        this.bold_r = bold_r;
    }

    public List<BigInteger> getBold_c() {
        return bold_c;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermutationCommitment that = (PermutationCommitment) o;
        return Objects.equals(bold_c, that.bold_c) &&
                Objects.equals(bold_r, that.bold_r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold_c, bold_r);
    }
}
