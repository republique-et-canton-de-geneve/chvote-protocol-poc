package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.PrimeField;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.*;

/**
 * This class holds the parameters and the methods / algorithms applicable to polynomials
 */
public class Polynomial {
    private final RandomGenerator randomGenerator;
    private final PrimeField primeField;

    public Polynomial(RandomGenerator randomGenerator, PrimeField primeField) {
        this.randomGenerator = randomGenerator;
        this.primeField = primeField;
    }

    /**
     * Algorithm 5.12: GenPoints
     *
     * @param n the vector containing the number of candidates per election
     * @param k the vector containing the number of allowed selections per election
     * @return a list of <i>n</i> random points picked from <i>t</i> different polynomials, along with the image of 0 for each polynomial
     */
    public PointsAndZeroImages genPoints(List<Integer> n, List<Integer> k) {
        Preconditions.checkArgument(n.size() == k.size(),
                String.format("|n| [%d] != |k| [%d]", n.size(), k.size()));
        for (int i = 0; i < n.size(); i++) {
            Preconditions.checkArgument(n.get(i) > k.get(i),
                    String.format("n_%1$d [%2$d] <= k_%1$d [%3$d]", i, n.get(i), k.get(i)));
        }
        List<Point> points = new ArrayList<>();
        List<BigInteger> y0s = new ArrayList<>();
        // i = 0 (used as subscript for x_i, y_i)
        // loop on election: index j (hence the a_j symbol)
        for (int i = 0; i < n.size(); i++) {
            Set<BigInteger> xValues = new HashSet<>();
            List<BigInteger> a_j = genPolynomial(k.get(i) - 1);
            for (int l = 0; l < n.get(i); l++) {
                BigInteger x_i;
                do {
                    x_i = randomGenerator.randomBigInteger(primeField.getP_prime());
                } while (x_i.compareTo(BigInteger.ZERO) == 0 || xValues.contains(x_i));
                xValues.add(x_i);
                BigInteger y_i = getYValue(x_i, a_j);
                points.add(new Point(x_i, y_i));
                // i++
            }
            y0s.add(getYValue(BigInteger.ZERO, a_j));
        }
        return new PointsAndZeroImages(points, y0s);
    }

    /**
     * Algorithm 5.13: GenPolynomial
     *
     * @param d the degree of the polynomial (-1 means a 0 constant)
     * @return the list of coefficients of a random polynomial p(X) = \sum(i=1,d){a_i*X^i mod p'}
     */
    public List<BigInteger> genPolynomial(int d) {
        Preconditions.checkArgument(d >= -1, String.format("Value of d should be greater or equal to -1 (it is [%d]", d));
        List<BigInteger> coefficients = new ArrayList<>();
        if (d == -1) {
            coefficients.add(BigInteger.ZERO);
        } else {
            for (int i = 0; i <= d; i++) {
                coefficients.add(randomGenerator.randomBigInteger(primeField.getP_prime()));
            }
        }
        return coefficients;
    }

    /**
     * Algorithm 5.14: GetYValue
     *
     * @param x value in Z_p_prime
     * @param a the coefficients of the polynomial
     * @return the computed value y
     */
    public BigInteger getYValue(BigInteger x, List<BigInteger> a) {
        Preconditions.checkArgument(a.size() >= 1,
                String.format("The size of a should always be larger or equal to 1 (it is [%d]", a.size()));
        if (x.equals(BigInteger.ZERO)) {
            return a.get(0);
        } else {
            BigInteger y = BigInteger.ZERO;
            for (BigInteger a_i : Lists.reverse(a)) {
                y = a_i.add(x.multiply(y).mod(primeField.getP_prime())).mod(primeField.getP_prime());
            }
            return y;
        }
    }

    public static class Point implements Hash.Hashable {
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

    public static class PointsAndZeroImages {
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
}
