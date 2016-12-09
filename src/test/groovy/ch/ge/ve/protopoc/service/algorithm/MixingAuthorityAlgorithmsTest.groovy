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
        encryptionGroup.q >> SEVEN
        encryptionGroup.g >> TWO
        encryptionGroup.h >> THREE

        mixingAuthorityAlgorithms = new MixingAuthorityAlgorithms(publicParameters, generalAlgorithms, voteConfirmationAuthorityAlgorithms, randomGenerator)
    }

    def "getEncryptions should retrieve a list of valid, confirmed encryptions"() {
        given:
        def B = [
                new BallotEntry(1, new BallotAndQuery(null, [TWO, FOUR, SIX], ONE, null), null),
                new BallotEntry(2, new BallotAndQuery(null, [THREE, FIVE, ONE], TWO, null), null),
                new BallotEntry(3, new BallotAndQuery(null, [TWO, FIVE, THREE], FOUR, null), null),
                new BallotEntry(6, new BallotAndQuery(null, [ONE, SIX, SEVEN], EIGHT, null), null)
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
                new Encryption(FOUR, ONE),
                new Encryption(EIGHT, FOUR)
        ])
    }

    def "genShuffle should generate a valid shuffle"() {
        given:
        randomGenerator.randomIntInRange(_, _) >>> [1, 1, 2] // psy = [1, 0, 2]
        randomGenerator.randomInZq(SEVEN) >>> [SIX, FOUR, TWO]
        def bold_e = [
                new Encryption(FIVE, TWO),
                new Encryption(THREE, ONE),
                new Encryption(FIVE, NINE)
        ]
        def publicKey = new EncryptionPublicKey(THREE, encryptionGroup)

        expect:
        mixingAuthorityAlgorithms.genShuffle(bold_e, publicKey) == new Shuffle(
                [
                        new Encryption(ONE, FIVE),
                        new Encryption(FOUR, SEVEN),
                        new Encryption(ONE, THREE)
                ],
                [SIX, FOUR, TWO],
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
        randomGenerator.randomInZq(SEVEN) >> r_prime

        expect:
        mixingAuthorityAlgorithms.genReEncryption(new Encryption(a, b), pk) ==
                new ReEncryption(new Encryption(a_prime, b_prime), r_prime)

        where:
        a     | b   | r_prime || a_prime | b_prime
        FIVE  | TWO | SIX     || FOUR    | SEVEN
        THREE | ONE | FOUR    || ONE     | FIVE
    }

    def "genShuffleProof should generate a valid shuffle proof"() {
        given:
        def bold_e = [
                new Encryption(FIVE, TWO),
                new Encryption(THREE, ONE),
                new Encryption(FIVE, NINE)
        ]
        def bold_e_prime = [
                new Encryption(ONE, FIVE),
                new Encryption(FOUR, SEVEN),
                new Encryption(ONE, THREE)
        ]
        def bold_r_prime = [SIX, FOUR, TWO]
        def psy = [1, 0, 2]
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)
        generalAlgorithms.getGenerators(3) >> [TWO, THREE, FIVE]
        randomGenerator.randomInZq(SEVEN) >>> [
                ONE, // genPermutationCommitment, r_1
                TWO, // genPermutationCommitment, r_2
                THREE, // genPermutationCommitment, r_3
                FOUR, // genCommitmentChain, r_circ_1
                FIVE, // genCommitmentChain, r_circ_2
                SIX, // genCommitmentChain, r_circ_3
                ONE, // omega_1
                TWO, // omega_2
                THREE, // omega_3
                FOUR, // omega_4
                TWO, // omega_circ_1
                THREE, // omega_prime_1
                FOUR, // omega_circ_2
                FIVE, // omega_prime_2
                SIX, // omega_circ_3
                ONE, // omega_prime_3
        ]
        generalAlgorithms.getChallenges(3, [bold_e, bold_e_prime, [SIX, EIGHT, SEVEN]] as List[], SEVEN) >>
                [TWO, FOUR, SIX]
        generalAlgorithms.getNIZKPChallenge(_, _, _) >> FIVE

        when:
        def proof = mixingAuthorityAlgorithms.genShuffleProof(bold_e, bold_e_prime, bold_r_prime, psy, pk)

        then: "the values in the proof match those computed by hand"
        proof.t.t_1 == TWO
        proof.t.t_2 == FOUR
        proof.t.t_3 == ONE
        proof.t.t_4 == [THREE, TWO]
        proof.t.t_circ == [NINE, FIVE, EIGHT]
        proof.s.s_1 == THREE
        proof.s.s_2 == ZERO
        proof.s.s_3 == THREE
        proof.s.s_4 == ONE
        proof.s.s_circ == [ONE, ONE, ONE]
        proof.s.s_prime == [TWO, ONE, THREE]
        proof.bold_c == [SIX, EIGHT, SEVEN]
        proof.bold_c_prime == [NINE, SEVEN, THREE]
    }

    def "genPermutationCommitment should generate a valid permutation commitment"() {
        given:
        randomGenerator.randomInZq(SEVEN) >>> random

        expect:
        mixingAuthorityAlgorithms.genPermutationCommitment(psy, bold_h) == new PermutationCommitment(bold_c, random)

        where:
        psy       | bold_h             | random            || bold_c
        [1, 0, 2] | [TWO, THREE, FIVE] | [ONE, TWO, THREE] || [SIX, EIGHT, SEVEN]
    }

    def "genCommitmentChain should generate a valid commitment chain"() {
        given:
        randomGenerator.randomInZq(SEVEN) >>> bold_r

        expect:
        mixingAuthorityAlgorithms.genCommitmentChain(THREE, bold_u_prime) == new CommitmentChain(bold_c, bold_r)

        where:
        bold_u_prime       | bold_r            || bold_c
        [FIVE, ONE, THREE] | [ONE, TWO, THREE] || [TWO, EIGHT, FOUR]
    }
}
