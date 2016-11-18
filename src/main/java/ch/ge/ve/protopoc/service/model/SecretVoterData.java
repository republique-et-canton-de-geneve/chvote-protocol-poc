package ch.ge.ve.protopoc.service.model;

import com.google.common.base.MoreObjects;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Model class holding the secret data for a voter
 */
public class SecretVoterData {
    private final BigInteger x;
    private final BigInteger y;
    private final byte[] F;
    private final byte[][] rc;

    public SecretVoterData(BigInteger x, BigInteger y, byte[] f, byte[][] rc) {
        this.x = x;
        this.y = y;
        F = f;
        this.rc = rc;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public byte[] getF() {
        return F;
    }

    public byte[][] getRc() {
        return rc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecretVoterData that = (SecretVoterData) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                Arrays.equals(F, that.F) &&
                Arrays.deepEquals(rc, that.rc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, F, rc);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .add("F", F)
                .add("rc", rc)
                .toString();
    }
}
