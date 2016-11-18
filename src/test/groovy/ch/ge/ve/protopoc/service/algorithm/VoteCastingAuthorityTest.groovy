package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE

/**
 * Tests on the vote casting algorithms on the authority side
 */
class VoteCastingAuthorityTest extends Specification {
    def PublicParameters publicParameters = Mock()
    def EncryptionGroup encryptionGroup = Mock()
    def IdentificationGroup identificationGroup = Mock()
    def GeneralAlgorithms generalAlgorithms = Mock()
    def RandomGenerator randomGenerator = Mock()
    def Hash hash = Mock()

    def VoteCastingAuthority voteCastingAuthority

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> SEVEN
        encryptionGroup.q >> THREE
        encryptionGroup.g >> TWO
        publicParameters.identificationGroup >> identificationGroup
        identificationGroup.p_circ >> ELEVEN
        identificationGroup.q_circ >> FIVE
        identificationGroup.g_circ >> THREE

        voteCastingAuthority = new VoteCastingAuthority(publicParameters, generalAlgorithms, randomGenerator, hash)
    }

    def "hasBallot should detect if a BallotEntry list contains a given voter index"() {
        def ballotList = [
                new BallotEntry(3, null, null),
                new BallotEntry(1, null, null),
                new BallotEntry(45, null, null)
        ]

        expect:
        result == voteCastingAuthority.hasBallot(i, ballotList)

        where:
        i  || result
        1  || true
        2  || false
        3  || true
        4  || false
        44 || false
        45 || true
        46 || false
    }

    def "checkBallotNIZKP should verify the validity of a provided proof"() {
        given:
        def encryptionKey = new EncryptionPublicKey(ONE, encryptionGroup)
        generalAlgorithms.getNIZKPChallenge([x_circ, a, b] as BigInteger[], t as BigInteger[], THREE) >> c

        expect:
        result == voteCastingAuthority.checkBallotNIZKP(new NonInteractiveZKP(t, s), x_circ, a, b, encryptionKey)

        where:
        t                 | s                | x_circ | a    | b    | c    || result
        [NINE, SIX, TWO]  | [TWO, FIVE, TWO] | THREE  | FOUR | FOUR | FIVE || true
        [NINE, FIVE, TWO] | [TWO, FIVE, TWO] | THREE  | FOUR | FOUR | FIVE || false
        [NINE, SIX, TWO]  | [TWO, FOUR, TWO] | THREE  | FOUR | FOUR | FIVE || false

    }
}
