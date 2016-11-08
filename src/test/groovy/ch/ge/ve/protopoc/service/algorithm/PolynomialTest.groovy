package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.Election
import ch.ge.ve.protopoc.service.model.PrimeField
import ch.ge.ve.protopoc.service.support.BigIntegers
import spock.lang.Specification

import java.security.SecureRandom

/**
 * Unit tests for the Polynomial class
 */
class PolynomialTest extends Specification {
    def SecureRandom secureRandom = Mock()
    def PrimeField primeField

    def Polynomial polynomial

    void setup() {
        primeField = new PrimeField(BigIntegers.SEVEN)
        polynomial = new Polynomial(secureRandom, primeField)
    }

    def "genPoints should generate a random polynomial by election and compute its value at random points, as well as the image of 0"() {
        given: "a single election with a 1-out-of-3 choice (typical referendum setting)"
        def election = new Election(3, 1, null)

        when: "generating points for an election"
        def pointsAndZeroes = polynomial.genPoints([election])

        then: "the proper number of random elements are created"
        secureRandom.nextBytes(_) >> {
            def bytes = it[0] as byte[]
            bytes[0] = 3 // called by genPolynomial
        } >> {
            def bytes = it[0] as byte[]
            bytes[0] = 2 // called by randomBigInteger (first candidate)
        } >> {
            def bytes = it[0] as byte[]
            bytes[0] = 0 // called by randomBigInteger (second candidate) --> discarded, is 0
        } >> {
            def bytes = it[0] as byte[]
            bytes[0] = 4 // called by randomBigInteger (second candidate)
        } >> {
            def bytes = it[0] as byte[]
            bytes[0] = 4 // called by randomBigInteger (third candidate) --> discarded, already in existing set
        } >> {
            def bytes = it[0] as byte[]
            bytes[0] = 5 // called by randomBigInteger (third and last candidate)
        }
        and: "the candidate points match the expected elements"
        def pointCand1 = new Polynomial.Point(BigIntegers.TWO, BigIntegers.THREE);
        def pointCand2 = new Polynomial.Point(BigIntegers.FOUR, BigIntegers.THREE);
        def pointCand3 = new Polynomial.Point(BigIntegers.FIVE, BigIntegers.THREE);
        pointsAndZeroes.getPoints().size() == 3
        pointsAndZeroes.getPoints().containsAll([pointCand1, pointCand2, pointCand3])

        and: "the is a single 0 point (because there is a single polynomial, for a single election)"
        pointsAndZeroes.getY0s().size() == 1
        pointsAndZeroes.getY0s().contains(BigIntegers.THREE)

        and: "the equality method works"
        pointsAndZeroes == new Polynomial.PointsAndZeroImages([pointCand1, pointCand2, pointCand3], [BigIntegers.THREE])
    }

    def "genPolynomial should generate a polynomial of the requested size"() {
        // With d = -1
        expect: "genPolynomial(-1) should return an array with the 0 constant"
        polynomial.genPolynomial(-1) == [BigInteger.ZERO]

        // Test with d = 0
        when: "genPolynomial(0) should"
        def poly0 = polynomial.genPolynomial(0)
        then: "generate a random BigInteger"
        1 * secureRandom.nextBytes(_) >> {
            def bytes = it[0] as byte[]
            bytes[0] = 5
        }
        and: "return it as constant"
        poly0 == [BigIntegers.FIVE]

        // With d = 1
        when: "genPolynomial(1) should"
        def poly1 = polynomial.genPolynomial(1)
        then: "generate two random BigIntegers"
        secureRandom.nextBytes(_) >> {
            def bytes = it[0] as byte[]
            bytes[0] = 3
        } >> {
            def bytes = it[0] as byte[]
            bytes[0] = 2
        }
        and: "return them as coefficients"
        poly1 == [BigIntegers.THREE, BigIntegers.TWO]
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
