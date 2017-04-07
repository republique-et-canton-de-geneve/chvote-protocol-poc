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

import ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic
import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests on the mixing algorithms
 */
class MixingAuthorityAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()
    VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms = Mock()
    RandomGenerator randomGenerator = Mock()

    EncryptionGroup encryptionGroup = Mock()

    MixingAuthorityAlgorithms mixingAuthorityAlgorithms
    DecryptionAuthorityAlgorithms decryptionAuthorityAlgorithms

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE
        encryptionGroup.h >> FOUR

        mixingAuthorityAlgorithms = new MixingAuthorityAlgorithms(publicParameters, generalAlgorithms, voteConfirmationAuthorityAlgorithms, randomGenerator)
        decryptionAuthorityAlgorithms = new DecryptionAuthorityAlgorithms(publicParameters, generalAlgorithms, randomGenerator)
    }

    def "getEncryptions should retrieve a list of valid, confirmed encryptions"() {
        given:
        def B = [
                new BallotEntry(1, new BallotAndQuery(null, [ONE, FOUR, NINE], ONE, null), null),
                new BallotEntry(2, new BallotAndQuery(null, [THREE, FIVE, ONE], THREE, null), null),
                new BallotEntry(3, new BallotAndQuery(null, [FOUR, FIVE, THREE], FOUR, null), null),
                new BallotEntry(6, new BallotAndQuery(null, [ONE, NINE, FIVE], NINE, null), null)
        ]
        def C = [
                new ConfirmationEntry(1, null),
                new ConfirmationEntry(3, null),
                new ConfirmationEntry(5, null)
        ]
        voteConfirmationAuthorityAlgorithms.hasConfirmation(1, C) >> true
        voteConfirmationAuthorityAlgorithms.hasConfirmation(2, C) >> false
        voteConfirmationAuthorityAlgorithms.hasConfirmation(3, C) >> true
        voteConfirmationAuthorityAlgorithms.hasConfirmation(6, C) >> false

        expect:
        mixingAuthorityAlgorithms.getEncryptions(B, C).containsAll([
                new Encryption(THREE, ONE),
                new Encryption(FIVE, FOUR)
        ])
    }

    def "genShuffle should generate a valid shuffle"() {
        given:
        randomGenerator.randomIntInRange(_, _) >>> [1, 1, 2] // psy = [1, 0, 2]
        randomGenerator.randomInZq(FIVE) >>> [ONE, TWO, FOUR]
        def bold_e = [
                new Encryption(FIVE, ONE),
                new Encryption(THREE, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def publicKey = new EncryptionPublicKey(THREE, encryptionGroup)

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true

        when:
        def shuffle = mixingAuthorityAlgorithms.genShuffle(bold_e, publicKey)

        then:
        shuffle.bold_e_prime.size() == 3
        shuffle.bold_r_prime.size() == 3
        shuffle.bold_r_prime.containsAll([ONE, TWO, FOUR]) // making the shuffle parallel made the
        // test run order unpredictable
        shuffle.psy == [1, 0, 2]

        def p = ELEVEN
        def pk = THREE
        def g = THREE
        def r_0 = shuffle.bold_r_prime.get(0)
        def r_1 = shuffle.bold_r_prime.get(1)
        def r_2 = shuffle.bold_r_prime.get(2)
        def e_prime_0 = shuffle.bold_e_prime.get(0)
        def e_prime_1 = shuffle.bold_e_prime.get(1)
        def e_prime_2 = shuffle.bold_e_prime.get(2)

        e_prime_0.a == (THREE * pk.modPow(r_1, p)) % p
        e_prime_0.b == (FOUR * g.modPow(r_1, p)) % p
        e_prime_1.a == (FIVE * pk.modPow(r_0, p)) % p
        e_prime_1.b == (ONE * g.modPow(r_0, p)) % p
        e_prime_2.a == (FIVE * pk.modPow(r_2, p)) % p
        e_prime_2.b == (NINE * pk.modPow(r_2, p)) % p
    }

    def "genPermutation should generate a valid permutation"() {
        given:
        randomGenerator.randomIntInRange(_, _) >>> randomInts

        expect:
        mixingAuthorityAlgorithms.genPermutation(n) == psy

        where:
        n | randomInts   || psy
        1 | [0]          || [0]
        2 | [1, 1]       || [1, 0]
        3 | [1, 1, 2]    || [1, 0, 2]
        4 | [0, 3, 2, 3] || [0, 3, 2, 1]
    }

    def "genReEncryption should correctly re-encrypt the ballot"() {
        given:
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)
        randomGenerator.randomInZq(FIVE) >> r_prime

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(a) >> true
        generalAlgorithms.isMember(b) >> true

        expect:
        mixingAuthorityAlgorithms.genReEncryption(new Encryption(a, b), pk) ==
                new ReEncryption(new Encryption(a_prime, b_prime), r_prime)

        where:
        a     | b    | r_prime || a_prime | b_prime
        FIVE  | NINE | SIX     || FOUR    | FIVE
        THREE | ONE  | FOUR    || ONE     | FOUR
    }

    def "genShuffleProof should generate a valid shuffle proof"() {
        given:
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
        def bold_r_prime = [ONE, FOUR, TWO]
        def psy = [1, 0, 2]
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)
        generalAlgorithms.getGenerators(3) >> [FOUR, THREE, FIVE]
        randomGenerator.randomInZq(FIVE) >>> [
                ONE, // genPermutationCommitment, r_?
                TWO, // genPermutationCommitment, r_?
                THREE, // genPermutationCommitment, r_?
                FOUR, // genCommitmentChain, r_circ_1
                ZERO, // genCommitmentChain, r_circ_2
                ONE, // genCommitmentChain, r_circ_3
                ONE, // omega_1
                TWO, // omega_2
                THREE, // omega_3
                FOUR, // omega_4
                TWO, // omega_circ/prime_?
                THREE, // omega_circ/prime_?
                FOUR, // omega_circ/prime_?
                ZERO, // omega_circ/prime_?
                ONE, // omega_circ/prime_?
                ONE, // omega_circ/prime_?
        ]
        generalAlgorithms.getChallenges(3, _ as Object[], FIVE) >>
                [TWO, FOUR, THREE]
        generalAlgorithms.getNIZKPChallenge(_, _, _) >> FOUR

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(ONE) >> true
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }

        when:
        def proof = mixingAuthorityAlgorithms.genShuffleProof(bold_e, bold_e_prime, bold_r_prime, psy, pk)

        then: "the proof should be valid"
        decryptionAuthorityAlgorithms.checkShuffleProof(proof, bold_e, bold_e_prime, pk)
    }

    def "genPermutationCommitment should generate a valid permutation commitment"() {
        given:
        randomGenerator.randomInZq(FIVE) >>> random

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true

        when:
        def commitment = mixingAuthorityAlgorithms.genPermutationCommitment(psy, bold_h)

        then:
        commitment.bold_r.containsAll(random)
        commitment.bold_c.get(0) == BigIntegerArithmetic.modExp(THREE, commitment.bold_r.get(0), ELEVEN)
                .multiply(bold_h.get(psy.get(0))).mod(ELEVEN)
        commitment.bold_c.get(1) == BigIntegerArithmetic.modExp(THREE, commitment.bold_r.get(1), ELEVEN)
                .multiply(bold_h.get(psy.get(1))).mod(ELEVEN)
        commitment.bold_c.get(2) == BigIntegerArithmetic.modExp(THREE, commitment.bold_r.get(2), ELEVEN)
                .multiply(bold_h.get(psy.get(2))).mod(ELEVEN)

        where:
        psy       | bold_h              | random            || bold_c
        [1, 0, 2] | [FOUR, THREE, FIVE] | [ONE, TWO, THREE] || [NINE, THREE, THREE]
    }

    def "genCommitmentChain should generate a valid commitment chain"() {
        given:
        randomGenerator.randomInZq(FIVE) >>> bold_r

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }
        generalAlgorithms.isInZ_q_circ(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < identificationGroup.q_circ }

        expect:
        mixingAuthorityAlgorithms.genCommitmentChain(FOUR, bold_u) == new CommitmentChain(bold_c, bold_r)

        where:
        bold_u             | bold_r            || bold_c
        [FOUR, TWO, THREE] | [FOUR, ZERO, ONE] || [ONE, ONE, THREE]
    }
}
