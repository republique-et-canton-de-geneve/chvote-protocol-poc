package ch.ge.ve.protopoc.service.model.polynomial;

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class combining a list of points and y-images of 0 for a set of polynomials
 */
public class PointsAndZeroImages {
    private final List<Point> points;
    private final List<BigInteger> y0s;

    public PointsAndZeroImages(List<Point> points, List<BigInteger> y0s) {
        this.points = ImmutableList.copyOf(points);
        this.y0s = ImmutableList.copyOf(y0s);
    }

    public List<Point> getPoints() {
        return ImmutableList.copyOf(points);
    }

    public List<BigInteger> getY0s() {
        return ImmutableList.copyOf(y0s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointsAndZeroImages that = (PointsAndZeroImages) o;
        return Objects.equals(points, that.points) &&
                Objects.equals(y0s, that.y0s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, y0s);
    }
}
