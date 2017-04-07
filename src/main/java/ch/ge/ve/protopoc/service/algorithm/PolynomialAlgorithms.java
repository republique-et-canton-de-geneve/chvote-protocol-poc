/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - chvote-protocol-poc                                                                            -
 - %%                                                                                             -
 - Copyright (C) 2016 - 2017 République et Canton de Genève                                       -
 - %%                                                                                             -
 - This program is free software: you can redistribute it and/or modify                           -
 - it under the terms of the GNU Affero General Public License as published by                    -
 - the Free Software Foundation, either version 3 of the License, or                              -
 - (at your option) any later version.                                                            -
 -                                                                                                -
 - This program is distributed in the hope that it will be useful,                                -
 - but WITHOUT ANY WARRANTY; without even the implied warranty of                                 -
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                                   -
 - GNU General Public License for more details.                                                   -
 -                                                                                                -
 - You should have received a copy of the GNU Affero General Public License                       -
 - along with this program. If not, see <http://www.gnu.org/licenses/>.                           -
 - #L%                                                                                            -
 -------------------------------------------------------------------------------------------------*/

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
     * Algorithm 7.7: GenPoints
     *
     * @param bold_n the vector containing the number of candidates per election
     * @param bold_k the vector containing the number of allowed selections per election
     * @return a list of <i>bold_n</i> random points picked from <i>t</i> different polynomials, along with the image of 0 for each polynomial
     */
    public PointsAndZeroImages genPoints(List<Integer> bold_n, List<Integer> bold_k) {
        Preconditions.checkArgument(bold_n.size() == bold_k.size(),
                String.format("|bold_n| [%d] != |bold_k| [%d]", bold_n.size(), bold_k.size()));
        for (int i = 0; i < bold_n.size(); i++) {
            Preconditions.checkArgument(bold_n.get(i) > bold_k.get(i),
                    String.format("n_%1$d [%2$d] <= k_%1$d [%3$d]", i, bold_n.get(i), bold_k.get(i)));
        }
        List<Point> bold_p = new ArrayList<>();
        List<BigInteger> bold_y = new ArrayList<>();
        int i = 0; // (used as subscript for x_i, y_i)
        // loop on election: index j (hence the a_j symbol)
        for (int j = 0; j < bold_n.size(); j++) {
            Set<BigInteger> upper_x = new HashSet<>();
            List<BigInteger> bold_a_j = genPolynomial(bold_k.get(j) - 1);
            for (int l = 0; l < bold_n.get(j); l++) {
                BigInteger x;
                do {
                    x = randomGenerator.randomInZq(primeField.getP_prime());
                } while (x.compareTo(BigInteger.ZERO) == 0 || upper_x.contains(x));
                upper_x.add(x);
                BigInteger y = getYValue(x, bold_a_j);
                Point p_i = new Point(x, y);
                bold_p.add(p_i);
                log.debug(String.format("Created point %d: %s", i, p_i));
                i++;
            }
            bold_y.add(getYValue(BigInteger.ZERO, bold_a_j));
        }
        return new PointsAndZeroImages(bold_p, bold_y);
    }

    /**
     * Algorithm 7.8: GenPolynomial
     *
     * @param d the degree of the polynomial (-1 means a 0 constant)
     * @return the list of coefficients of a random polynomial p(X) = \sum(i=1,d){a_i*X^i mod p'}
     */
    public List<BigInteger> genPolynomial(int d) {
        Preconditions.checkArgument(d >= -1, String.format("Value of d should be greater or equal to -1 (it is [%d]", d));
        List<BigInteger> bold_a = new ArrayList<>();
        if (d == -1) {
            bold_a.add(BigInteger.ZERO);
        } else {
            for (int i = 0; i <= d - 1; i++) {
                bold_a.add(randomGenerator.randomInZq(primeField.getP_prime()));
            }
            // a_d \isin Z_p_prime \ {0}
            bold_a.add(randomGenerator.
                    // random in range 0 - p'-2
                            randomInZq(primeField.getP_prime().subtract(BigInteger.ONE))
                    // --> random in range 1 - p'-1
                    .add(BigInteger.ONE));
        }
        return bold_a;
    }

    /**
     * Algorithm 7.9: GetYValue
     * <p>Generates the coefficients a_0, ..., a_d of a random polynomial</p>
     *
     * @param x value in Z_p_prime
     * @param bold_a the coefficients of the polynomial
     * @return the computed value y
     */
    public BigInteger getYValue(BigInteger x, List<BigInteger> bold_a) {
        Preconditions.checkArgument(bold_a.size() >= 1,
                String.format("The size of bold_a should always be larger or equal to 1 (it is [%d]", bold_a.size()));
        if (x.equals(BigInteger.ZERO)) {
            return bold_a.get(0);
        } else {
            BigInteger y = BigInteger.ZERO;
            for (BigInteger a_i : Lists.reverse(bold_a)) {
                y = a_i.add(x.multiply(y).mod(primeField.getP_prime())).mod(primeField.getP_prime());
            }
            return y;
        }
    }

}
