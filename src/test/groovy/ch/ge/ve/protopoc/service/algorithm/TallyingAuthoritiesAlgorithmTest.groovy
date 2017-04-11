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

package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.exception.TallyingRuntimeException
import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.simulation.SimulationConstants
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the algorithms performed during the tallying phase
 */
class TallyingAuthoritiesAlgorithmTest extends Specification {
    // Primary Mocks
    GeneralAlgorithms generalAlgorithms = Mock()

    def defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray() as List<Character>
    EncryptionGroup encryptionGroup = new EncryptionGroup(ELEVEN, FIVE, THREE, FOUR)
    IdentificationGroup identificationGroup = new IdentificationGroup(ELEVEN, FIVE, THREE)
    SecurityParameters securityParameters = new SecurityParameters(1, 1, 2, 0.99)
    PrimeField primeField = new PrimeField(SEVEN)
    PublicParameters publicParameters = new PublicParameters(
            securityParameters, encryptionGroup, identificationGroup, primeField,
            FIVE, defaultAlphabet, FIVE, defaultAlphabet,
            defaultAlphabet, 2, defaultAlphabet, 2, 2, 5
    )
    SecurityParameters securityParameters = Mock()

    // Class under test
    TallyingAuthoritiesAlgorithm tallyingAuthoritiesAlgorithm

    void setup() {
        tallyingAuthoritiesAlgorithm = new TallyingAuthoritiesAlgorithm(publicParameters, generalAlgorithms)
    }

    def "checkDecryptionProofs should validate the proofs for all authorities"() {
        given: "Some input data"
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
        and: "the expected preconditions checks"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }

        expect: "the decryption proofs check to succeed"
        tallyingAuthoritiesAlgorithm.checkDecryptionProofs(bold_pi_prime, bold_pk, bold_e, bold_B_prime)
    }

    def "checkDecryptionProof should correctly validate an authority's partial decryption proof"() {
        given: "Some input data"
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
        and: "the expected preconditions checks"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }

        expect: "The check of a single decryption proof to succeed"
        tallyingAuthoritiesAlgorithm.checkDecryptionProof(pi_prime, pk_j, bold_e, bold_b_prime)
    }

    def "getDecryptions should properly retrieve the original plaintext messages"() {
        given: "Some input data"
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
        and: "the expected preconditions checks"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }

        expect: "The decryption to be successful and have the expected result"
        tallyingAuthoritiesAlgorithm.getDecryptions(bold_e, bold_B_prime) == [FIVE, FIVE, FIVE, THREE]
    }

    def "getTally should get a valid tally count"() {
        given: "A slightly larger encryption group"
        def otherEncryptionGroup = new EncryptionGroup(SimulationConstants.p_RC0e, SimulationConstants.q_RC0e,
                SimulationConstants.g_RC0e, SimulationConstants.h_RC0e)
        def otherPublicParameters = new PublicParameters(
                securityParameters, otherEncryptionGroup, identificationGroup, primeField,
                FIVE, defaultAlphabet, FIVE, defaultAlphabet,
                defaultAlphabet, 2, defaultAlphabet, 2, 4, 5
        )
        def otherTallyingAuthoritiesAlgorithm = new TallyingAuthoritiesAlgorithm(otherPublicParameters,
                generalAlgorithms)

        and: "Some primes"
        // The primes in G_83 : [2, 3, 7, 11, 19, 29, 31, 47, 61, 89, 97, 107, 127, 137, 157]
        // The group is too small to encode 7 * 29... thus the choices are restricted to smaller values
        generalAlgorithms.getPrimes(6) >> [2, 3, 7, 11, 19, 29].collect { BigInteger.valueOf(it) }

        and: "some sample m values"
        def m = [2 * 11, 2 * 19, 2 * 29, 3 * 19, 7 * 11].collect { BigInteger.valueOf(it) }

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(BigInteger.valueOf(2 * 11)) >> true
        generalAlgorithms.isMember(BigInteger.valueOf(2 * 19)) >> true
        generalAlgorithms.isMember(BigInteger.valueOf(2 * 29)) >> true
        generalAlgorithms.isMember(BigInteger.valueOf(3 * 19)) >> true
        generalAlgorithms.isMember(BigInteger.valueOf(7 * 11)) >> true

        expect:
        otherTallyingAuthoritiesAlgorithm.getVotes(m, 6) == [
                [true, false, false, true, false, false],
                [true, false, false, false, true, false],
                [true, false, false, false, false, true],
                [false, true, false, false, true, false],
                [false, false, true, true, false, false]
        ] as List<List<Boolean>>
    }

    def "getTally should fail if the group is too small for the requested number of primes"() {
        given: "some mock parameters"
        List<BigInteger> m = [ONE]

        and: "an exception thrown when trying to get some primes"
        generalAlgorithms.getPrimes(_) >> { throw new NotEnoughPrimesInGroupException("p is too small") }

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(ONE) >> true

        when:
        tallyingAuthoritiesAlgorithm.getVotes(m, 200)

        then:
        thrown(TallyingRuntimeException)

    }
}
