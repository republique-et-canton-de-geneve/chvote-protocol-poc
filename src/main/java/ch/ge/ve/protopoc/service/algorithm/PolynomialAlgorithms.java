package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.PrimeField;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import ch.ge.ve.protopoc.service.model.polynomial.PointsAndZeroImages;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class holds the parameters and the methods / algorithms applicable to polynomials
 */
public class PolynomialAlgorithms {
    private static final Logger log = LoggerFactory.getLogger(PolynomialAlgorithms.class);
    private final RandomGenerator randomGenerator;
    private final PrimeField primeField;


    public PolynomialAlgorithms(RandomGenerator randomGenerator, PrimeField primeField) {
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
        int i = 0; // (used as subscript for x_i, y_i)
        // loop on election: index j (hence the a_j symbol)
        for (int j = 0; j < n.size(); j++) {
            Set<BigInteger> xValues = new HashSet<>();
            List<BigInteger> a_j = genPolynomial(k.get(j) - 1);
            for (int l = 0; l < n.get(j); l++) {
                BigInteger x_i;
                do {
                    x_i = randomGenerator.randomInZq(primeField.getP_prime());
                } while (x_i.compareTo(BigInteger.ZERO) == 0 || xValues.contains(x_i));
                xValues.add(x_i);
                BigInteger y_i = getYValue(x_i, a_j);
                Point point = new Point(x_i, y_i);
                points.add(point);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Created point %d: %s", i, point));
                }
                i++;
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
            for (int i = 0; i <= d - 1; i++) {
                coefficients.add(randomGenerator.randomInZq(primeField.getP_prime()));
            }
            // a_d \isin Z_p_prime \ {0}
            coefficients.add(randomGenerator.
                    // random in range 0 - p'-2
                            randomInZq(primeField.getP_prime().subtract(BigInteger.ONE))
                    // --> random in range 1 - p'-1
                    .add(BigInteger.ONE));
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

}
