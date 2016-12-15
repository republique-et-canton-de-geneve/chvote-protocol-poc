package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.exception.TallyingRuntimeException
import ch.ge.ve.protopoc.service.model.DecryptionProof
import ch.ge.ve.protopoc.service.model.Encryption
import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.model.PublicParameters
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the algorithms performed during the tallying phase
 */
class TallyingAuthoritiesAlgorithmTest extends Specification {
    // Primary Mocks
    PublicParameters publicParameters = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()

    // Secondary Mocks
    EncryptionGroup encryptionGroup = Mock()

    // Class under test
    TallyingAuthoritiesAlgorithm tallyingAuthoritiesAlgorithm

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        publicParameters.s >> 2

        tallyingAuthoritiesAlgorithm = new TallyingAuthoritiesAlgorithm(publicParameters, generalAlgorithms)
    }

    def "checkDecryptionProofs should validate the proofs for all authorities"() {
        given: "A very small encryption group"
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE

        and: "Some input data"
        def bold_pi_prime = [
                new DecryptionProof([NINE, THREE, NINE, FIVE, FOUR], ZERO),
                new DecryptionProof([FOUR, NINE, FOUR, THREE, FIVE], ONE)
        ]
        def bold_pk = [FIVE, THREE]
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FOUR, NINE)
        ]
        def bold_B_prime = [
                [FOUR, FIVE, NINE, THREE],
                [FIVE, THREE, FOUR, NINE]
        ]
        generalAlgorithms.getNIZKPChallenge(*_) >>> [ONE, TWO]

        expect: "the decryption proofs check to succeed"
        tallyingAuthoritiesAlgorithm.checkDecryptionProofs(bold_pi_prime, bold_pk, bold_e, bold_B_prime)
    }

    def "checkDecryptionProof should correctly validate an authority's partial decryption proof"() {
        given: "A very small encryption group"
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE

        and: "Some input data"
        def pi_prime = new DecryptionProof([NINE, THREE, NINE, FIVE, FOUR], ZERO)
        def pk_j = FIVE
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FOUR, NINE)
        ]
        def bold_b_prime = [FOUR, FIVE, NINE, THREE]
        generalAlgorithms.getNIZKPChallenge(*_) >> ONE

        expect: "The check of a single decryption proof to succeed"
        tallyingAuthoritiesAlgorithm.checkDecryptionProof(pi_prime, pk_j, bold_e, bold_b_prime)
    }

    def "getDecryptions should properly retrieve the original plaintext messages"() {
        given: "A very small encryption group"
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE

        and: "Some input data"
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FOUR, NINE)
        ]
        def bold_B_prime = [
                [FOUR, FIVE, NINE, THREE],
                [FIVE, THREE, FOUR, NINE]
        ]

        expect: "The decryption to be successful and have the expected result"
        tallyingAuthoritiesAlgorithm.getDecryptions(bold_e, bold_B_prime) == [FIVE, FIVE, FIVE, THREE]
    }

    def "getTally should get a valid tally count"() {
        given: "A small encryption group"
        encryptionGroup.p >> BigInteger.valueOf(167L)

        and: "Some primes"
        // The primes in G_83 : [2, 3, 7, 11, 19, 29, 31, 47, 61, 89, 97, 107, 127, 137, 157]
        // The group is too small to encode 7 * 29... thus the choices are restricted to smaller values
        generalAlgorithms.getPrimes(6) >> [2, 3, 7, 11, 19, 29].collect { BigInteger.valueOf(it) }

        and: "some sample m values"
        def m = [2 * 11, 2 * 19, 2 * 29, 3 * 19, 7 * 11].collect { BigInteger.valueOf(it) }

        expect:
        tallyingAuthoritiesAlgorithm.getTally(m, 6) == [3, 1, 1, 2, 2, 1] as List<Long>
    }

    def "getTally should fail if the group is too small for the requested number of primes"() {
        given: "some mock parameters"
        List<BigInteger> m = Mock()

        and: "an exception thrown when trying to get some primes"
        generalAlgorithms.getPrimes(_) >> { throw new NotEnoughPrimesInGroupException("p is too small") }

        when:
        tallyingAuthoritiesAlgorithm.getTally(m, 200)

        then:
        thrown(TallyingRuntimeException)

    }
}
