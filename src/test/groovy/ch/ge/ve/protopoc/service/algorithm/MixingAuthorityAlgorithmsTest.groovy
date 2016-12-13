package ch.ge.ve.protopoc.service.algorithm

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

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE
        encryptionGroup.h >> FOUR

        mixingAuthorityAlgorithms = new MixingAuthorityAlgorithms(publicParameters, generalAlgorithms, voteConfirmationAuthorityAlgorithms, randomGenerator)
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
        randomGenerator.randomInZq(FIVE) >>> [ONE, FOUR, TWO]
        def bold_e = [
                new Encryption(FIVE, ONE),
                new Encryption(THREE, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def publicKey = new EncryptionPublicKey(THREE, encryptionGroup)

        expect:
        mixingAuthorityAlgorithms.genShuffle(bold_e, publicKey) == new Shuffle(
                [
                        new Encryption(ONE, FIVE),
                        new Encryption(FOUR, THREE),
                        new Encryption(ONE, FOUR)
                ],
                [ONE, FOUR, TWO],
                [1, 0, 2]
        )

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
                ONE, // genPermutationCommitment, r_1
                TWO, // genPermutationCommitment, r_2
                THREE, // genPermutationCommitment, r_3
                FOUR, // genCommitmentChain, r_circ_1
                ZERO, // genCommitmentChain, r_circ_2
                ONE, // genCommitmentChain, r_circ_3
                ONE, // omega_1
                TWO, // omega_2
                THREE, // omega_3
                FOUR, // omega_4
                TWO, // omega_circ_1
                THREE, // omega_prime_1
                FOUR, // omega_circ_2
                ZERO, // omega_prime_2
                ONE, // omega_circ_3
                ONE, // omega_prime_3
        ]
        generalAlgorithms.getChallenges(3, [bold_e, bold_e_prime, [NINE, THREE, THREE]] as List[], FIVE) >>
                [TWO, FOUR, THREE]
        generalAlgorithms.getNIZKPChallenge(_, _, _) >> FOUR

        when:
        def proof = mixingAuthorityAlgorithms.genShuffleProof(bold_e, bold_e_prime, bold_r_prime, psy, pk)

        then: "the values in the proof match those computed by hand"
        proof.t.t_1 == THREE
        proof.t.t_2 == NINE
        proof.t.t_3 == FIVE
        proof.t.t_4 == [THREE, FOUR]
        proof.t.t_circ == [FOUR, FOUR, THREE]
        proof.s.s_1 == ZERO
        proof.s.s_2 == TWO
        proof.s.s_3 == FOUR
        proof.s.s_4 == ZERO
        proof.s.s_circ == [THREE, FOUR, ZERO]
        proof.s.s_prime == [FOUR, THREE, THREE]
        proof.bold_c == [NINE, THREE, THREE]
        proof.bold_c_circ == [ONE, ONE, THREE]
    }

    def "checkShuffleProof should correctly validate a shuffle proof"() {
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
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)
        def t = new ShuffleProof.T(THREE, NINE, FIVE, [THREE, FOUR], [FOUR, FOUR, THREE])
        def s = new ShuffleProof.S(ZERO, TWO, FOUR, ZERO, [THREE, FOUR, ZERO], [FOUR, THREE, THREE])
        def bold_c = [NINE, THREE, THREE]
        def bold_c_circ = [ONE, ONE, THREE]
        def pi = new ShuffleProof(t, s, bold_c, bold_c_circ)

        generalAlgorithms.getGenerators(3) >> [FOUR, THREE, FIVE]
        generalAlgorithms.getChallenges(3, [bold_e, bold_e_prime, [NINE, THREE, THREE]] as List[], FIVE) >>
                [TWO, FOUR, THREE]
        generalAlgorithms.getNIZKPChallenge(_, _, _) >> FOUR

        expect:
        mixingAuthorityAlgorithms.checkShuffleProof(pi, bold_e, bold_e_prime, pk) // == true implied
    }

    def "genPermutationCommitment should generate a valid permutation commitment"() {
        given:
        randomGenerator.randomInZq(FIVE) >>> random

        expect:
        mixingAuthorityAlgorithms.genPermutationCommitment(psy, bold_h) == new PermutationCommitment(bold_c, random)

        where:
        psy       | bold_h              | random            || bold_c
        [1, 0, 2] | [FOUR, THREE, FIVE] | [ONE, TWO, THREE] || [NINE, THREE, THREE]
    }

    def "genCommitmentChain should generate a valid commitment chain"() {
        given:
        randomGenerator.randomInZq(FIVE) >>> bold_r

        expect:
        mixingAuthorityAlgorithms.genCommitmentChain(FOUR, bold_u_prime) == new CommitmentChain(bold_c, bold_r)

        where:
        bold_u_prime       | bold_r            || bold_c
        [FOUR, TWO, THREE] | [FOUR, ZERO, ONE] || [ONE, ONE, THREE]
    }
}
