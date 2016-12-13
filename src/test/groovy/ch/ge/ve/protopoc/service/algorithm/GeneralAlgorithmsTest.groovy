package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.support.Conversion
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.JacobiSymbol
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.TEN

/**
 * This specification defines the expected behaviour of the general algorithms
 */
class GeneralAlgorithmsTest extends Specification {
    GeneralAlgorithms generalAlgorithms
    JacobiSymbol jacobiSymbol = Mock()
    Hash hash = Mock()
    Conversion conversion = new Conversion()

    def static ELEVEN = BigInteger.valueOf(11L)

    EncryptionGroup eg = Mock()

    void setup() {
        generalAlgorithms = new GeneralAlgorithms(jacobiSymbol, hash, conversion, eg)

        eg.p >> ELEVEN
        eg.q >> FIVE
        eg.g >> THREE
        eg.h >> FIVE
    }

    def "isMember"() {
        when:
        jacobiSymbol.computeJacobiSymbol(ONE, ELEVEN) >> 1

        then:
        generalAlgorithms.isMember(x) == result

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
        def primes = generalAlgorithms.getPrimes(4)

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
        generalAlgorithms.getPrimes(5)

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
        def selectedPrimes = generalAlgorithms.getSelectedPrimes(Arrays.asList(1, 2, 4))

        then:
        selectedPrimes.size() == 3
        selectedPrimes.containsAll(TWO, THREE, SEVEN)
    }

    def "getGenerators"() {
        when:
        def generators = generalAlgorithms.getGenerators(3)

        then:
        4 * hash.hash(_ as Object[]) >>> [
                [0x04] as byte[],
                [0x05] as byte[],
                [0x01] as byte[],
                [0x02] as byte[]
        ]
        generators.containsAll(FIVE, THREE, FOUR)
    }

    def "getProofChallenge"() {
        Object[] v, t
        v = new Object[0]
        t = new Object[0]

        when:
        def challenge = generalAlgorithms.getNIZKPChallenge(v, t, ELEVEN)

        then:
        1 * hash.hash(v, t) >> ([0x0E] as byte[])
        challenge == THREE
    }

    def "getPublicChallenges"() {
        Object[] v = new Object[0]

        when:
        def challenges = generalAlgorithms.getChallenges(3, v, ELEVEN)

        then:
        1 * hash.hash(v, ONE) >> ([0x0A] as byte[])
        1 * hash.hash(v, TWO) >> ([0x03] as byte[])
        1 * hash.hash(v, THREE) >> ([0x1F] as byte[])

        challenges.containsAll(TEN, THREE, BigInteger.valueOf(9L))
    }
}
