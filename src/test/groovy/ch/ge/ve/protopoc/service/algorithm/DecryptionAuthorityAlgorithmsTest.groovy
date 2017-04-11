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

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the algorithms performed during the decryption phase
 */
class DecryptionAuthorityAlgorithmsTest extends Specification {
    // Primary Mocks
    GeneralAlgorithms generalAlgorithms = Mock()
    RandomGenerator randomGenerator = Mock()

    def defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray() as List<Character>
    EncryptionGroup encryptionGroup = new EncryptionGroup(ELEVEN, FIVE, THREE, FOUR)
    IdentificationGroup identificationGroup = new IdentificationGroup(ELEVEN, FIVE, THREE)
    SecurityParameters securityParameters = new SecurityParameters(1, 1, 2, 0.99)
    PrimeField primeField = new PrimeField(ELEVEN)
    PublicParameters publicParameters = new PublicParameters(
            securityParameters, encryptionGroup, identificationGroup, primeField,
            FIVE, defaultAlphabet, FIVE, defaultAlphabet,
            defaultAlphabet, 2, defaultAlphabet, 2, 2, 3
    )
    SecurityParameters securityParameters = Mock()

    // Class under test
    DecryptionAuthorityAlgorithms decryptionAuthorityAlgorithms

    void setup() {
        decryptionAuthorityAlgorithms = new DecryptionAuthorityAlgorithms(publicParameters, generalAlgorithms, randomGenerator)

    }

    def "checkShuffleProofs should check the shuffles performed by the other authorities"() {
        given: "a series of encryptions"
        def e_0 = [
                new Encryption(FIVE, ONE),
                new Encryption(THREE, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def e_1 = [
                new Encryption(ONE, FIVE),
                new Encryption(FOUR, THREE),
                new Encryption(ONE, FOUR)
        ]
        def e_2 = [
                new Encryption(NINE, FIVE),
                new Encryption(ONE, NINE),
                new Encryption(FOUR, THREE)
        ]
        def bold_E = [e_1, e_2]

        and: "a public key"
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)

        and: "a valid shuffle proof"
        def t = new ShuffleProof.T(THREE, NINE, FIVE, [THREE, FOUR], [FOUR, FOUR, FOUR])
        def s = new ShuffleProof.S(ONE, TWO, THREE, FOUR, [TWO, FOUR, ONE], [THREE, ZERO, ONE])
        def bold_c = [NINE, THREE, THREE]
        def bold_c_hat = [FOUR, FIVE, ONE]
        def pi = new ShuffleProof(t, s, bold_c, bold_c_hat)

        def bold_pi = [pi, null]

        and: "some mocked collaborators"
        generalAlgorithms.getGenerators(3) >> [FOUR, THREE, FIVE]
        generalAlgorithms.getChallenges(3, [e_0, e_1, [NINE, THREE, THREE]] as List[], 1) >>
                [TWO, ZERO, THREE]
        generalAlgorithms.getNIZKPChallenge(_, _, 1) >> ZERO

        and: "the expected preconditions"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger it -> 0 <= it && it < encryptionGroup.q }

        and: "an authority index"
        int j = 1

        expect:
        //noinspection GroovyPointlessBoolean
        decryptionAuthorityAlgorithms.checkShuffleProofs(bold_pi, e_0, bold_E, pk, j) == true
    }

    def "checkShuffleProofs should fail given an invalid proof"() {
        given: "a series of encryptions"
        def e_0 = [
                new Encryption(FIVE, ONE),
                new Encryption(THREE, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def e_1 = [
                new Encryption(ONE, FIVE),
                new Encryption(FOUR, THREE),
                new Encryption(ONE, FOUR)
        ]
        def e_2 = [
                new Encryption(NINE, FIVE),
                new Encryption(ONE, NINE),
                new Encryption(FOUR, THREE)
        ]
        def bold_E = [e_1, e_2]

        and: "a public key"
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)

        and: "an invalid shuffle proof"
        def t = new ShuffleProof.T(FOUR /* invalid data */, NINE, FIVE, [THREE, FOUR], [FOUR, FOUR, FOUR])
        def s = new ShuffleProof.S(ONE, TWO, THREE, FOUR, [TWO, FOUR, ONE], [THREE, ZERO, ONE])
        def bold_c = [NINE, THREE, THREE]
        def bold_c_hat = [FOUR, FIVE, ONE]
        def pi = new ShuffleProof(t, s, bold_c, bold_c_hat)

        def bold_pi = [pi, null]

        and: "some mocked collaborators"
        generalAlgorithms.getGenerators(3) >> [FOUR, THREE, FIVE]
        generalAlgorithms.getChallenges(3, [e_0, e_1, [NINE, THREE, THREE]] as List[], 1) >>
                [TWO, ZERO, THREE]
        generalAlgorithms.getNIZKPChallenge(_, _, 1) >> ZERO

        and: "the expected preconditions"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger it -> 0 <= it && it < encryptionGroup.q }

        and: "an authority index"
        int j = 1

        expect:
        !decryptionAuthorityAlgorithms.checkShuffleProofs(bold_pi, e_0, bold_E, pk, j)
    }

    def "checkShuffleProof should correctly validate a shuffle proof"() {
        given: "some input"
        def bold_e = [
                new Encryption(FIVE, ONE),
                new Encryption(THREE, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def bold_e_prime = [
                new Encryption(ONE, FIVE),
                new Encryption(FOUR, THREE),
                new Encryption(ONE, FOUR)
        ]
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)
        def t = new ShuffleProof.T(THREE, NINE, FIVE, [THREE, FOUR], [FOUR, FOUR, FOUR])
        def s = new ShuffleProof.S(ONE, TWO, THREE, FOUR, [TWO, FOUR, ONE], [THREE, ZERO, ONE])
        def bold_c = [NINE, THREE, THREE]
        def bold_c_hat = [FOUR, FIVE, ONE]
        def pi = new ShuffleProof(t, s, bold_c, bold_c_hat)

        and: "some mocked collaborators"
        generalAlgorithms.getGenerators(3) >> [FOUR, THREE, FIVE]
        generalAlgorithms.getChallenges(3, [bold_e, bold_e_prime, [NINE, THREE, THREE]] as List[], 1) >>
                [TWO, ZERO, THREE]
        generalAlgorithms.getNIZKPChallenge(_, _, 1) >> ZERO

        and: "the expected preconditions"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger it -> 0 <= it && it < encryptionGroup.q }

        expect:
        //noinspection GroovyPointlessBoolean
        decryptionAuthorityAlgorithms.checkShuffleProof(pi, bold_e, bold_e_prime, pk) == true
    }

    def "getPartialDecryptions should perform partial decryptions on provided encryptions"() {
        given:
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def sk_j = THREE

        and: "the expected preconditions"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true

        expect:
        decryptionAuthorityAlgorithms.getPartialDecryptions(bold_e, sk_j) == [FOUR, FIVE, NINE, THREE]
    }

    def "genDecryptionProof should generate a valid decryption proof"() {
        given:
        def sk_j = THREE
        def pk_j = FIVE
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def bold_b_prime = [FOUR, FIVE, NINE, THREE]
        randomGenerator.randomInZq(FIVE) >> TWO
        generalAlgorithms.getNIZKPChallenge(*_) >> ONE

        and: "the expected preconditions"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }

        expect:
        decryptionAuthorityAlgorithms.genDecryptionProof(sk_j, pk_j, bold_e, bold_b_prime) ==
                new DecryptionProof([NINE, THREE, NINE, FIVE, FOUR], ZERO)
    }
}
