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
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the algorithms related to the code sheets preparation
 */
class VotingCardPreparationAlgorithmsTest extends Specification {
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

    VotingCardPreparationAlgorithms codeSheetPreparation

    void setup() {
        codeSheetPreparation = new VotingCardPreparationAlgorithms(publicParameters)
    }

    def "getVotingCards should combine the secret voter data in the expected manner"() {
        given:
        def domainOfInfluence1 = new DomainOfInfluence("test 1")
        def domainOfInfluence2 = new DomainOfInfluence("test 2")
        Voter voter1 = new Voter()
        Voter voter2 = new Voter()
        and: "the first election is a 1-out-of-2 election (typical referendum)"
        Election election1 = new Election(2, 1, domainOfInfluence1)
        and: "the second election is a 1-out-of-2 election"
        Election election2 = new Election(2, 1, domainOfInfluence2)

        and: "both voters are eligible for the first election"
        voter1.addDomainsOfInfluence(domainOfInfluence1)
        voter2.addDomainsOfInfluence(domainOfInfluence1)
        and: "only the first voter is eligible for the second election"
        voter1.addDomainsOfInfluence(domainOfInfluence2)

        and: "some candidates"
        Candidate candidate1 = new Candidate("1")
        Candidate candidate2 = new Candidate("2")
        Candidate candidate3 = new Candidate("3")
        Candidate candidate4 = new Candidate("4")

        def electionSet = new ElectionSet([voter1, voter2], [candidate1, candidate2, candidate3, candidate4],
                [election1, election2])

        and: "the following values for voter 1"
        SecretVoterData voter1Authority1 = new SecretVoterData(ZERO, TWO, [0x01, 0x01] as byte[],
                [[0x01, 0x02], [0x03, 0x04], [0x05, 0x06], [0x07, 0x08]] as byte[][])
        SecretVoterData voter1Authority2 = new SecretVoterData(TWO, ONE, [0x11, 0x11] as byte[],
                [[0xC1, 0xD2], [0xE3, 0xF4], [0xC5, 0xD6], [0xE7, 0xF8]] as byte[][])

        and: "the following values for voter 2"

        SecretVoterData voter2Authority1 = new SecretVoterData(FOUR, ONE, [0x0F, 0x0F] as byte[],
                [[0x01, 0x02], [0x03, 0x04], [0x05, 0x06], [0x07, 0x08]] as byte[][])
        SecretVoterData voter2Authority2 = new SecretVoterData(ZERO, ONE, [0x31, 0x41] as byte[],
                [[0xC0, 0xD0], [0xE0, 0xF0], [0xC0, 0xD0], [0xE0, 0xF0]] as byte[][])

        when:
        def sheets = codeSheetPreparation.getVotingCard(
                electionSet,
                [
                        [voter1Authority1, voter2Authority1], // Authority 1; i.e. bold_d_1
                        [voter1Authority2, voter2Authority2] // Authority 2; i.e. bold_d_2
                ])

        then:
        def sheet_1 = sheets.get(0)
        sheet_1.voter == voter1
        sheet_1.electionSet == electionSet
        sheet_1.upper_x == "c" // 0 + 2 = 2
        sheet_1.upper_y == "d" // 2 + 1 = 3
        sheet_1.upper_fc == "baq" // [0x10, 0x10] -> 4112
        sheet_1.bold_rc == [
                "mdq", // [0xC0, 0xD0] -> marked with 0, n_max = 3 -> [0xC0, 0xD0] -> 49360
                "ohW", // [0xE0, 0xF0] -> marked with 1, n_max = 3 -> [0xE1, 0xF0] -> 57840
                "mdr", // [0xC0, 0xD0] -> marked with 2, n_max = 3 -> [0xC0, 0xD1] -> 49361
                "ohX" // [0xE0, 0xF0] -> marked with 3, n_max = 3 -> [0xE1, 0xF1] -> 57841
        ]
        sheet_1.bold_k == [1, 1]

        def sheet_2 = sheets.get(1)
        sheet_2.voter == voter2
        sheet_2.electionSet == electionSet
        sheet_2.upper_x == "e" // 4 + 0 = 4
        sheet_2.upper_y == "c" // 1 + 1 = 2
        sheet_2.upper_fc == "d5o" // [0x3E, 0x4E] -> 15950
        sheet_2.bold_rc == [
                "mds", // [0xC1, 0xD2] -> marked with 0, n_max = 3 -> [0xC0, 0xD2] -> 49362
                "op0", // [0xE3, 0xF4] -> marked with 1, n_max = 3 -> [0xE3, 0xF4] -> 58356
                "mtx", // [0xC5, 0xD6] -> marked with 2, n_max= 3 -> [0xC4, 0xD7] -> 50391
                "oF5" // [0xE7, 0xF8] -> marked with 2, n_max= 3 -> [0xE7, 0xF9] -> 59385
        ]
        sheet_2.bold_k == [1, 0]
    }
}
