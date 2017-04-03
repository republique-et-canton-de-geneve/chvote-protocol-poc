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

import ch.ge.ve.protopoc.service.exception.BallotNotFoundException
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
class VoteConfirmationAuthorityAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()
    EncryptionGroup encryptionGroup = Mock()
    IdentificationGroup identificationGroup = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()
    VoteCastingAuthorityAlgorithms voteCastingAuthority = Mock()
    Hash hash = Mock()
    PrimeField primeField = Mock()

    VoteConfirmationAuthorityAlgorithms voteConfirmationAuthority


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
        primeField.p_prime >> SEVEN
        publicParameters.s >> 4
        publicParameters.upper_l_f >> 2

        voteConfirmationAuthority =
                new VoteConfirmationAuthorityAlgorithms(publicParameters, generalAlgorithms, voteCastingAuthority, hash)
    }

    def "checkConfirmation should verify if a given confirmation is valid"() {
        given: "a list of public credentials"
        def bold_y_circ = [THREE, ONE, NINE, FOUR]
        and: "a mocked ballot list"
        List<BallotEntry> ballotList = Mock()
        and: "a confirmation list"
        def confirmationList = [
                new ConfirmationEntry(2, null)
        ]
        and: "some ballot presence verifications"
        voteCastingAuthority.hasBallot(0, ballotList) >> false
        voteCastingAuthority.hasBallot(1, ballotList) >> true
        voteCastingAuthority.hasBallot(2, ballotList) >> true
        voteCastingAuthority.hasBallot(3, ballotList) >> true
        and: "the following proof challenges"
        generalAlgorithms.getNIZKPChallenge([y_circ] as BigInteger[], t as BigInteger[], FIVE) >> THREE

        and: "the following constructed parameters"
        def pi = new NonInteractiveZKP(t, s)
        def gamma = new Confirmation(y_circ, pi)

        and: "the expected preconditions checks"
        generalAlgorithms.isMember_G_q_circ(t[0]) >> true
        generalAlgorithms.isMember_G_q_circ(y_circ) >> true
        generalAlgorithms.isInZ_q_circ(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < identificationGroup.q_circ }

        expect:
        voteConfirmationAuthority.checkConfirmation(i, gamma, bold_y_circ, ballotList, confirmationList) == result

        where:
        i | y_circ | t       | s       || result
        0 | THREE  | [THREE] | [TWO]   || false // hasBallot(0, B) is false
        1 | ONE    | [FIVE]  | [THREE] || true // everything should be ok
        2 | NINE   | [THREE] | [FIVE]  || false // hasConfirmation(2, C) is true -->
        3 | FOUR   | [FIVE]  | [THREE] || false // the proof is not valid
    }

    def "hasConfirmation should find matching confirmations from the list"() {
        given: "a list of confirmations"
        def C = [
                new ConfirmationEntry(0, null),
                new ConfirmationEntry(2, null),
                new ConfirmationEntry(10, null)
        ]

        expect:
        voteConfirmationAuthority.hasConfirmation(i, C) == result

        where:
        i   || result
        0   || true
        1   || false
        2   || true
        3   || false
        9   || false
        10  || true
        11  || false
        100 || false
    }

    def "checkConfirmationProof should correctly validate the confirmation proof"() {
        given:
        generalAlgorithms.getNIZKPChallenge([y_circ] as BigInteger[], t as BigInteger[], FIVE) >> THREE

        and: "the expected preconditions checks"
        generalAlgorithms.isMember_G_q_circ(t[0]) >> true
        generalAlgorithms.isMember_G_q_circ(y_circ) >> true
        generalAlgorithms.isInZ_q_circ(_ as BigInteger) >> { BigInteger x -> 0 <= x && x < identificationGroup.q_circ }

        expect:
        voteConfirmationAuthority.checkConfirmationProof(new NonInteractiveZKP(t, s), y_circ) == result

        where:
        t      | s       | y_circ || result
        [FIVE] | [THREE] | ONE    || true
        [FOUR] | [THREE] | ONE    || false
        [FIVE] | [TWO]   | ONE    || false
        [FIVE] | [THREE] | THREE  || false
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
        hash.recHash_L(points) >> code

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
        and: "some random hash"
        hash.recHash_L(_) >> ([0x04, 0x01] as byte[])

        when: "a call to getFinalization is performed"
        voteConfirmationAuthority.getFinalization(2, pointMatrix, ballotList)

        then: "an exception should be thrown"
        thrown(BallotNotFoundException)
    }
}
