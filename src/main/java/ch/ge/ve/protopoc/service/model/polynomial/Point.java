package ch.ge.ve.protopoc.service.model.polynomial;

import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.base.MoreObjects;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Model class for a geometric point
 */
public class Point implements Hash.Hashable {
    public final BigInteger x;
    public final BigInteger y;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Object[] elementsToHash() {
        BigInteger[] elementsToHash = new BigInteger[2];
        elementsToHash[0] = x;
        elementsToHash[1] = y;
        return elementsToHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Objects.equals(x, point.x) &&
                Objects.equals(y, point.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .toString();
    }
}
