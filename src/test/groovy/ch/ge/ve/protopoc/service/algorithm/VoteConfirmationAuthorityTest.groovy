package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.model.polynomial.Point
import ch.ge.ve.protopoc.service.support.Hash
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests on the vote confirmation algorithms performed by the authorities
 */
class VoteConfirmationAuthorityTest extends Specification {
    def PublicParameters publicParameters = Mock()
    def EncryptionGroup encryptionGroup = Mock()
    def IdentificationGroup identificationGroup = Mock()
    def GeneralAlgorithms generalAlgorithms = Mock()
    def VoteCastingAuthority voteCastingAuthority = Mock()
    def Hash hash = Mock()
    def PrimeField primeField = Mock()

    def VoteConfirmationAuthority voteConfirmationAuthority


    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> SEVEN
        encryptionGroup.q >> THREE
        encryptionGroup.g >> TWO
        publicParameters.identificationGroup >> identificationGroup
        identificationGroup.p_circ >> ELEVEN
        identificationGroup.q_circ >> FIVE
        identificationGroup.g_circ >> THREE
        publicParameters.primeField >> primeField
        primeField.p_prime >> SEVEN
        publicParameters.s >> 4

        voteConfirmationAuthority =
                new VoteConfirmationAuthority(publicParameters, generalAlgorithms, voteCastingAuthority, hash)
    }

    def "checkConfirmationNIZKP should correctly validate the confirmation NIZKP"() {
        given:
        generalAlgorithms.getNIZKPChallenge([y_circ] as BigInteger[], t as BigInteger[], FIVE) >> THREE

        expect:
        voteConfirmationAuthority.checkConfirmationNIZKP(new NonInteractiveZKP(t, s), y_circ) == result

        where:
        t      | s       | y_circ || result
        [FIVE] | [THREE] | ONE    || true
        [FOUR] | [THREE] | ONE    || false
        [FIVE] | [TWO]   | ONE    || false
        [FIVE] | [THREE] | TWO    || false
    }

    def "getFinalization should hash the correct points and return the adequate values"() {
        given: "a set of parameters"
        def pointMatrix = [
                [   // voter 0
                    new Point(ONE, THREE)
                ],
                [   // voter 1
                    new Point(TWO, ONE)
                ],
                [   // voter 2
                    new Point(FIVE, SIX)
                ]
        ]
        def ballotList = [
                new BallotEntry(0, null, [THREE, TWO]),
                new BallotEntry(1, null, [ZERO, ONE])
        ]
        and: "ballots are all found in the ballot list"
        voteCastingAuthority.hasBallot(i, ballotList) >> true
        and: "an fixed hash value"
        hash.hash(points) >> code

        expect:
        voteConfirmationAuthority.getFinalization(i, pointMatrix, ballotList) ==
                new FinalizationCodePart(code, bold_r)

        where:
        i | points                | bold_r       || code
        0 | new Point(ONE, THREE) | [THREE, TWO] || [0xAB, 0xCD] as byte[]
        1 | new Point(TWO, ONE)   | [ZERO, ONE]  || [0x10, 0x32] as byte[]
    }

    def "getFinalization should fail when the ballot is missing from the ballot list"() {
        given: "a set of parameters"
        def pointMatrix = [
                [   // voter 0
                    new Point(ONE, THREE)
                ],
                [   // voter 1
                    new Point(TWO, ONE)
                ],
                [   // voter 2
                    new Point(FIVE, SIX)
                ]
        ]
        def ballotList = [
                new BallotEntry(0, null, [THREE, TWO]),
                new BallotEntry(1, null, [ZERO, ONE])
        ]
        and: "ballots that are not found in the ballot list"
        voteCastingAuthority.hasBallot(2, ballotList) >> false

        when: "a call to getFinalization is performed"
        voteConfirmationAuthority.getFinalization(2, pointMatrix, ballotList)

        then: "an exception should be thrown"
        thrown(IllegalArgumentException)
    }
}
