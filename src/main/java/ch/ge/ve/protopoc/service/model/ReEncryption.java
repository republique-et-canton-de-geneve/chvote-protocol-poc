package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Model class used to represent a "re-encryption", i.e. the resulting encryption and the randomness used
 */
public class ReEncryption {
    private final Encryption encryption;
    private final BigInteger randomness;

    public ReEncryption(Encryption encryption, BigInteger randomness) {
        this.encryption = encryption;
        this.randomness = randomness;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public BigInteger getRandomness() {
        return randomness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReEncryption that = (ReEncryption) o;
        return Objects.equals(encryption, that.encryption) &&
                Objects.equals(randomness, that.randomness);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryption, randomness);
    }

    @Override
    public String toString() {
        return "ReEncryption{" +
                "encryption=" + encryption +
                ", randomness=" + randomness +
                '}';
    }
}
