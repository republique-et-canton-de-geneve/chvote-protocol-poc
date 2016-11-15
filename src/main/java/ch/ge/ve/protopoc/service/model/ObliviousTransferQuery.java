package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Model class for an Oblivious Transfer query
 */
public class ObliviousTransferQuery {
    private final List<BigInteger> bold_a;
    private final List<BigInteger> bold_r;

    public ObliviousTransferQuery(List<BigInteger> bold_a, List<BigInteger> bold_r) {
        this.bold_a = bold_a;
        this.bold_r = bold_r;
    }

    public List<BigInteger> getBold_a() {
        return bold_a;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }
}
