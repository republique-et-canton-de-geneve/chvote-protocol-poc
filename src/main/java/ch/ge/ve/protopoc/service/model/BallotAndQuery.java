package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Model class combining a ballot and a OT query
 */
public class BallotAndQuery {

    private final BigInteger x_circ;
    private final List<BigInteger> bold_a;
    private final BigInteger b;
    private final NonInteractiveZKP pi;

    public BallotAndQuery(BigInteger x_circ, List<BigInteger> bold_a, BigInteger b, NonInteractiveZKP pi) {
        this.x_circ = x_circ;
        this.bold_a = bold_a;
        this.b = b;
        this.pi = pi;
    }

    public BigInteger getX_circ() {
        return x_circ;
    }

    public List<BigInteger> getBold_a() {
        return bold_a;
    }

    public BigInteger getB() {
        return b;
    }

    public NonInteractiveZKP getPi() {
        return pi;
    }
}
