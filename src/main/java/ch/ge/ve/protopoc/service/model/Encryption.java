package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.collect.ComparisonChain;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Model class containing one encryption of one ballot
 */
public class Encryption implements Hash.Hashable, Comparable<Encryption> {
    private final BigInteger a;
    private final BigInteger b;

    public Encryption(BigInteger a, BigInteger b) {
        this.a = a;
        this.b = b;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Encryption that = (Encryption) o;
        return Objects.equals(a, that.a) &&
                Objects.equals(b, that.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "Encryption{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }

    @Override
    public Object[] elementsToHash() {
        return new BigInteger[]{a, b};
    }

    @Override
    public int compareTo(Encryption o) {
        return ComparisonChain.start()
                .compare(this.a, o.a)
                .compare(this.b, o.b)
                .result();
    }
}
