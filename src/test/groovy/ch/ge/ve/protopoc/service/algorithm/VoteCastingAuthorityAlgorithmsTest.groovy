package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.model.polynomial.Point
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE

/**
 * Tests on the vote casting algorithms on the authority side
 */
class VoteCastingAuthorityAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()
    EncryptionGroup encryptionGroup = Mock()
    IdentificationGroup identificationGroup = Mock()
    SecurityParameters securityParameters = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()
    RandomGenerator randomGenerator = Mock()
    Hash hash = Mock()

    VoteCastingAuthorityAlgorithms voteCastingAuthority

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE
        encryptionGroup.g >> THREE
        publicParameters.identificationGroup >> identificationGroup
        identificationGroup.p_circ >> ELEVEN
        identificationGroup.q_circ >> FIVE
        identificationGroup.g_circ >> THREE
        publicParameters.l_m >> 16
        publicParameters.securityParameters >> securityParameters
        securityParameters.l >> 16

        voteCastingAuthority = new VoteCastingAuthorityAlgorithms(publicParameters, generalAlgorithms, randomGenerator, hash)
    }

    def "checkBallot should correctly check the ballot"() {
        given:
        def encryptionKey = new EncryptionPublicKey(ONE, encryptionGroup)
        def ballotList = [
                new BallotEntry(3, null, null),
                new BallotEntry(1, null, null)
        ]
        List<BigInteger> publicCredentials = [THREE, FOUR, ONE, NINE]
        generalAlgorithms.getNIZKPChallenge(_ as BigInteger[], t as BigInteger[], FIVE) >> c

        expect:
        result == voteCastingAuthority.checkBallot(
                i,
                new BallotAndQuery(
                        x_circ,
                        bold_a,
                        b,
                        new NonInteractiveZKP(t, s)
                ),
                encryptionKey,
                publicCredentials,
                ballotList
        )

        where:
        i | x_circ | bold_a        | c    | b    | t                   | s                    || result
        0 | THREE  | [THREE, FOUR] | FOUR | FIVE | [FOUR, FIVE, THREE] | [THREE, FIVE, THREE] || true
        1 | THREE  | [THREE, FOUR] | FOUR | FIVE | [FOUR, FIVE, THREE] | [THREE, FIVE, THREE] || false
        0 | THREE  | [THREE, FOUR] | FOUR | FIVE | [FOUR, FIVE, NINE]  | [THREE, FIVE, THREE] || false
    }

    def "hasBallot should detect if a BallotEntry list contains a given voter index"() {
        given: "a ballot list"
        def ballotList = [
                new BallotEntry(3, null, null),
                new BallotEntry(1, null, null),
                new BallotEntry(45, null, null)
        ]

        expect: "the call to hasBallot to have the expected result"
        result == voteCastingAuthority.hasBallot(i, ballotList)

        where: "the values for i and the result are as follows"
        i  || result
        1  || true
        2  || false
        3  || true
        4  || false
        44 || false
        45 || true
        46 || false
    }

    def "checkBallotProof should verify the validity of a provided proof"() {
        given: "a fixed encryption key and challenge"
        def encryptionKey = new EncryptionPublicKey(ONE, encryptionGroup)
        generalAlgorithms.getNIZKPChallenge(_ as BigInteger[], t as BigInteger[], FIVE) >> c

        expect: "the verification of the Proof to have the expected result"
        result == voteCastingAuthority.checkBallotProof(new NonInteractiveZKP(t, s), x_circ, a, b, encryptionKey)

        where: "the values are taken from the following table"
        t                   | s                    | x_circ | a   | b    | c    || result
        [FOUR, FIVE, THREE] | [THREE, FIVE, THREE] | THREE  | ONE | FIVE | FOUR || true // values from genBallotProof test
        [NINE, FIVE, FOUR]  | [THREE, FIVE, THREE] | THREE  | ONE | FIVE | FOUR || false
        [FOUR, FIVE, THREE] | [THREE, FIVE, FOUR]  | THREE  | ONE | FIVE | FOUR || false
    }

    def "genResponse should generate a valid response to an OT query"() {
        given: "a fixed encryption key and challenge"
        def encryptionKey = new EncryptionPublicKey(ONE, encryptionGroup)
        List<Integer> candidatesNumberVector = [3]
        List<List<Integer>> selectionsMatrix = [[1], [1]]
        List<List<Point>> pointMatrix = [
                [   // voter1
                    new Point(ONE, SIX),
                    new Point(FOUR, SIX),
                    new Point(THREE, SIX)
                ],
                [   // voter2
                    new Point(TWO, THREE),
                    new Point(FIVE, THREE),
                    new Point(ONE, THREE)
                ]
        ]
        and: "some known randomess"
        randomGenerator.randomInZq(_) >> r
        and: "known primes"
        generalAlgorithms.getPrimes(3) >> [TWO, THREE, FIVE]
        and: "some hash values"
        hash.recHash_L(_) >>> [
                [0x00, 0x10], // l = 1
                [0x20, 0x30], // l = 2
                [0x40, 0x50] // l = 3
        ]

        expect: "the generated response should match the expected values"
        new ObliviousTransferResponseAndRand(
                new ObliviousTransferResponse(
                        bold_b, bold_c as byte[][], bold_d
                ), bold_r
        ) == voteCastingAuthority.genResponse(i, bold_a, encryptionKey, candidatesNumberVector, selectionsMatrix,
                pointMatrix)

        where: "the input / output values are"
        i | bold_a | r     | bold_b  | bold_c                                     | bold_d | bold_r
        0 | [FOUR] | THREE | [NINE]  | [[0x01, 0x16], [0x24, 0x36], [0x43, 0x56]] | [ONE]  | [THREE]
        1 | [FIVE] | TWO   | [THREE] | [[0x02, 0x13], [0x25, 0x33], [0x41, 0x53]] | [ONE]  | [TWO]
    }

    def "genResponse should fail if the group is too small"() {
        given: "a fixed encryption key and challenge"
        def pk = new EncryptionPublicKey(ONE, encryptionGroup)
        List<Integer> candidatesNumberVector = [3]
        List<List<Integer>> selectionsMatrix = [[1], [1]]
        List<List<Point>> pointMatrix = [
                [   // voter1
                    new Point(ONE, SIX),
                    new Point(FOUR, SIX),
                    new Point(THREE, SIX)
                ],
                [   // voter2
                    new Point(TWO, THREE),
                    new Point(FIVE, THREE),
                    new Point(ONE, THREE)
                ]
        ]
        and: "failure to get enough primes"
        generalAlgorithms.getPrimes(3) >> {
            args -> throw new NotEnoughPrimesInGroupException("not enough of them")
        }

        when: "an attempt is made at generating a response"
        voteCastingAuthority.genResponse(1, [ONE], pk, candidatesNumberVector, selectionsMatrix, pointMatrix)

        then:
        thrown(IncompatibleParametersException)
    }
}
