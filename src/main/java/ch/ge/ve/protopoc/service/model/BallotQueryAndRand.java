package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Model class combining a ballot, the corresponding OT query and the vector of random elements used
 */
public class BallotQueryAndRand {
    private final BallotAndQuery alpha;
    private final List<BigInteger> bold_r;

    public BallotQueryAndRand(BallotAndQuery alpha, List<BigInteger> bold_r) {
        this.alpha = alpha;
        this.bold_r = bold_r;
    }

    public BallotAndQuery getAlpha() {
        return alpha;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }
}
