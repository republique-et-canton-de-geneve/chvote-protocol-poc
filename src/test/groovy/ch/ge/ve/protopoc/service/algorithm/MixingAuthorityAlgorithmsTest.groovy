package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE

/**
 * Tests on the mixing algorithms
 */
class MixingAuthorityAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()
    EncryptionGroup encryptionGroup = Mock()
    RandomGenerator randomGenerator = Mock()
    VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms = Mock()

    MixingAuthorityAlgorithms mixingAuthorityAlgorithms

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> ELEVEN

        mixingAuthorityAlgorithms = new MixingAuthorityAlgorithms(publicParameters, voteConfirmationAuthorityAlgorithms, randomGenerator)
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
}
