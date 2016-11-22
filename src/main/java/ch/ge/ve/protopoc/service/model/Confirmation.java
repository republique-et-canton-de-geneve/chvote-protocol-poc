package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Model class containing the necessary information for confirmation of the vote
 */
public class Confirmation {
    private final BigInteger y_circ;
    private final NonInteractiveZKP pi;

    public Confirmation(BigInteger y_circ, NonInteractiveZKP pi) {
        this.y_circ = y_circ;
        this.pi = pi;
    }

    public BigInteger getY_circ() {
        return y_circ;
    }

    public NonInteractiveZKP getPi() {
        return pi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Confirmation that = (Confirmation) o;
        return Objects.equals(y_circ, that.y_circ) &&
                Objects.equals(pi, that.pi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(y_circ, pi);
    }

    @Override
    public String toString() {
        return String.format("Confirmation{y_circ=%s, pi=%s}", y_circ, pi);
    }
}
