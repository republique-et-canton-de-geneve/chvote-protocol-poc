package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;

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
}
