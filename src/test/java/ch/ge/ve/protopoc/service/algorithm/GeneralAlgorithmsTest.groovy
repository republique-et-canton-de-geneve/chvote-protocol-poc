package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.support.BigIntegers
import ch.ge.ve.protopoc.service.support.Conversion
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.JacobiSymbol
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.FIVE
import static ch.ge.ve.protopoc.service.support.BigIntegers.SEVEN
import static ch.ge.ve.protopoc.service.support.BigIntegers.THREE
import static ch.ge.ve.protopoc.service.support.BigIntegers.TWO
import static java.math.BigInteger.ONE

/**
 * This specification defines the expected behaviour of the general algorithms
 */
class GeneralAlgorithmsTest extends Specification {
    def GeneralAlgorithms generalAlgorithms
    def JacobiSymbol jacobiSymbol = Mock()
    def Hash hash = Mock()
    def Conversion conversion = Mock()

    def static ELEVEN = BigInteger.valueOf(11L)

    def EncryptionGroup eg = new EncryptionGroup(ELEVEN, null, null, null)

    void setup() {
        generalAlgorithms = new GeneralAlgorithms(jacobiSymbol, hash, conversion)
    }

    def "isMember"() {
        when:
        jacobiSymbol.computeJacobiSymbol(ONE, ELEVEN) >> 1

        then:
        generalAlgorithms.isMember(x, eg) == result

        where:
        x      | result
        ONE    | true
        ELEVEN | false
    }

    def "getPrimes"() {
        given:
        jacobiSymbol.computeJacobiSymbol(TWO, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(THREE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(FIVE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(SEVEN, ELEVEN) >> 1

        when:
        def primes = generalAlgorithms.getPrimes(4, eg)

        then:
        primes.size() == 4
        primes.containsAll(TWO, THREE, FIVE, SEVEN)
    }

    def "getPrimes not enough primes available"() {
        given:
        jacobiSymbol.computeJacobiSymbol(TWO, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(THREE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(FIVE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(SEVEN, ELEVEN) >> 1

        when:
        generalAlgorithms.getPrimes(5, eg)

        then:
        thrown(NotEnoughPrimesInGroupException)
    }

    def "getSelectedPrimes"() {
        given:
        jacobiSymbol.computeJacobiSymbol(TWO, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(THREE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(FIVE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(SEVEN, ELEVEN) >> 1

        when:
        def selectedPrimes = generalAlgorithms.getSelectedPrimes(Arrays.asList(1, 2, 4), eg)

        then:
        selectedPrimes.size() == 3;
        selectedPrimes.containsAll(TWO, THREE, SEVEN)
    }

    def "getGenerators"() {
        when:
        def generators = generalAlgorithms.getGenerators(3, eg)

        then:
        4 * conversion.toInteger(_) >>> [BigIntegers.FIVE, BigIntegers.THREE, BigInteger.ONE, BigIntegers.SEVEN]
        4 * hash.hash(_ as Object[]) >> ([] as byte[])
        generators.containsAll(FIVE, THREE, SEVEN)
    }
}
