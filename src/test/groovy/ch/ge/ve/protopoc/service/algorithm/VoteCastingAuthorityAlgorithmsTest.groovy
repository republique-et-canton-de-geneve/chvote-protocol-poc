/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - protocol-poc-back                                                                              -
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

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.model.polynomial.Point
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests on the vote casting algorithms on the authority side
 */
class VoteCastingAuthorityAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()
    EncryptionGroup encryptionGroup = Mock()
    IdentificationGroup identificationGroup = Mock()
    SecurityParameters securityParameters = Mock()
    PrimeField primeField = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()
    RandomGenerator randomGenerator = Mock()
    Hash hash = Mock()
    ElectionSet electionSet = Mock()

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
        publicParameters.primeField >> primeField
        primeField.p_prime >> ELEVEN
        publicParameters.l_m >> 16
        publicParameters.securityParameters >> securityParameters
        securityParameters.l >> 16

        voteCastingAuthority = new VoteCastingAuthorityAlgorithms(publicParameters, electionSet, generalAlgorithms, randomGenerator, hash)
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
        Voter voter0 = Mock()
        Voter voter1 = Mock()
        electionSet.voters >> [voter0, voter1]
        Election election = Mock()
        electionSet.elections >> [election]
        electionSet.isEligible(_ as Voter, election) >> true
        election.numberOfSelections >> 2

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isMember_G_q_circ(FOUR) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }
        generalAlgorithms.isInZ_q_circ(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < identificationGroup.q_circ }

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
        def encryptionKey = new EncryptionPublicKey(THREE, encryptionGroup)
        generalAlgorithms.getNIZKPChallenge(_ as BigInteger[], t as BigInteger[], FIVE) >> c

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(NINE) >> true
        generalAlgorithms.isMember_G_q_circ(FIVE) >> true
        generalAlgorithms.isMember_G_q_circ(NINE) >> true
        generalAlgorithms.isInZ_q(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < encryptionGroup.q }
        generalAlgorithms.isInZ_q_circ(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < identificationGroup.q_circ }

        expect: "the verification of the Proof to have the expected result"
        result == voteCastingAuthority.checkBallotProof(new NonInteractiveZKP(t, s), x_circ, a, b, encryptionKey)

        where: "the values are taken from the following table"
        t                   | s                    | x_circ | a    | b     | c    || result
        [FIVE, FOUR, THREE] | [THREE, NINE, ZERO]  | ONE    | NINE | THREE | FOUR || true // values from genBallotProof
        // test
        [NINE, FOUR, THREE] | [THREE, NINE, ZERO]  | ONE    | NINE | THREE | FOUR || false
        [FIVE, FOUR, THREE] | [THREE, NINE, THREE] | ONE    | NINE | THREE | FOUR || false
    }

    def "genResponse should generate a valid response to an OT query"() {
        given: "a fixed encryption key and challenge"
        def encryptionKey = new EncryptionPublicKey(THREE, encryptionGroup)
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

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(FOUR) >> true
        generalAlgorithms.isMember(FIVE) >> true

        expect: "the generated response should match the expected values"
        voteCastingAuthority.genResponse(i, bold_a, encryptionKey, candidatesNumberVector, selectionsMatrix,
                pointMatrix) ==
                new ObliviousTransferResponseAndRand(new ObliviousTransferResponse(
                        bold_b, bold_c as byte[][], bold_d
                ), bold_r)

        where: "the input / output values are"
        i | bold_a | r     | bold_b  | bold_c                                     | bold_d | bold_r
        0 | [FOUR] | THREE | [NINE]  | [[0x01, 0x16], [0x24, 0x36], [0x43, 0x56]] | [FIVE] | [THREE]
        1 | [FIVE] | TWO   | [THREE] | [[0x02, 0x13], [0x25, 0x33], [0x41, 0x53]] | [NINE] | [TWO]
    }

    def "genResponse should fail if the group is too small"() {
        given: "a fixed encryption key and challenge"
        def pk = new EncryptionPublicKey(THREE, encryptionGroup)
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

        and: "the expected preconditions checks"
        generalAlgorithms.isMember(THREE) >> true
        generalAlgorithms.isMember(ONE) >> true

        when: "an attempt is made at generating a response"
        voteCastingAuthority.genResponse(1, [ONE], pk, candidatesNumberVector, selectionsMatrix, pointMatrix)

        then:
        thrown(IncompatibleParametersException)
    }
}
