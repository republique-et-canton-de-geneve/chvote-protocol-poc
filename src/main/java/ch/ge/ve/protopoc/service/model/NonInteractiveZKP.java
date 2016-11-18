package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * A non-interactive ZKP
 */
public class NonInteractiveZKP {
    private final List<BigInteger> t;
    private final List<BigInteger> s;

    public NonInteractiveZKP(List<BigInteger> t, List<BigInteger> s) {
        this.t = t;
        this.s = s;
    }

    public List<BigInteger> getT() {
        return t;
    }

    public List<BigInteger> getS() {
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonInteractiveZKP that = (NonInteractiveZKP) o;
        return Objects.equals(t, that.t) &&
                Objects.equals(s, that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, s);
    }

    @Override
    public String toString() {
        return String.format("NonInteractiveZKP{t=%s, s=%s}", t, s);
    }
}
