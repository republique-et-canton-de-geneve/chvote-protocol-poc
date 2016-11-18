package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.PrimeField
import ch.ge.ve.protopoc.service.model.polynomial.Point
import ch.ge.ve.protopoc.service.model.polynomial.PointsAndZeroImages
import ch.ge.ve.protopoc.service.support.BigIntegers
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

/**
 * Unit tests for the Polynomial class
 */
class PolynomialTest extends Specification {
    def RandomGenerator randomGenerator = Mock()
    def PrimeField primeField

    def Polynomial polynomial

    void setup() {
        primeField = new PrimeField(BigIntegers.SEVEN)
        polynomial = new Polynomial(randomGenerator, primeField)
    }

    def "genPoints should generate a random polynomial by election and compute its value at random points, as well as the image of 0"() {
        when: "generating points for an election with a 1-out-of-3 choice (typical referendum setting)"
        def pointsAndZeroes = polynomial.genPoints([3], [1])

        then: "the proper number of random elements are created"
        randomGenerator.randomInZq(_) >>>
                [BigIntegers.TWO, // called by genPolynomial
                 BigIntegers.TWO, // called by randomBigInteger (first candidate)
                 BigInteger.ZERO, // called by randomBigInteger (second candidate) --> discarded, is 0
                 BigIntegers.FOUR, // called by randomBigInteger (second candidate)
                 BigIntegers.FOUR, // called by randomBigInteger (third candidate) --> discarded, already in existing set
                 BigIntegers.FIVE // called by randomBigInteger (third and last candidate)
                ]

        and: "the candidate points match the expected elements"
        def pointCand1 = new Point(BigIntegers.TWO, BigIntegers.THREE);
        def pointCand2 = new Point(BigIntegers.FOUR, BigIntegers.THREE);
        def pointCand3 = new Point(BigIntegers.FIVE, BigIntegers.THREE);
        pointsAndZeroes.getPoints().size() == 3
        pointsAndZeroes.getPoints().containsAll([pointCand1, pointCand2, pointCand3])

        and: "the is a single 0 point (because there is a single polynomial, for a single election)"
        pointsAndZeroes.getY0s().size() == 1
        pointsAndZeroes.getY0s().contains(BigIntegers.THREE)

        and: "the equality method works"
        pointsAndZeroes == new PointsAndZeroImages([pointCand1, pointCand2, pointCand3], [BigIntegers.THREE])
    }

    def "genPolynomial should generate a polynomial of the requested size"() {
        given:
        randomGenerator.randomInZq(_) >>> randomValues

        expect:
        a == polynomial.genPolynomial(d)

        where:
        d  | randomValues                       || a
        -1 | []                                 || [BigInteger.ZERO]
        0  | [BigIntegers.FOUR]                 || [BigIntegers.FIVE]
        1  | [BigIntegers.TWO, BigIntegers.TWO] || [BigIntegers.TWO, BigIntegers.THREE]

    }

    def "getYValue should compute image of value x"() {
        expect:
        y == polynomial.getYValue(x, a)

        where:
        y                 | x                 | a
        BigIntegers.THREE | BigIntegers.THREE | [BigIntegers.TWO, BigIntegers.FIVE] // 5 * 3 + 2 = 17; 17 mod 7 = 3
        BigInteger.ZERO   | BigIntegers.FOUR  | [BigInteger.ONE, BigIntegers.SEVEN, BigIntegers.THREE] // 3 * 4^2 + 7 * 4 + 1 = 77; 77 mod 7 = 0
    }
}
