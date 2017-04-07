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
class CodeSheetPreparationAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()
    IdentificationGroup identificationGroup = Mock()

    CodeSheetPreparationAlgorithms codeSheetPreparation

    void setup() {
        def defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray()

        publicParameters.s >> 2 // For the sake of simplifying the examples
        publicParameters.q_circ_x >> FIVE
        publicParameters.l_x >> 2
        publicParameters.q_circ_y >> FIVE
        publicParameters.l_y >> 2
        publicParameters.upper_l_f >> 2
        publicParameters.upper_l_r >> 2
        publicParameters.upper_a_x >> (defaultAlphabet as List<Character>)
        publicParameters.upper_a_y >> (defaultAlphabet as List<Character>)
        publicParameters.upper_a_f >> (defaultAlphabet as List<Character>)
        publicParameters.upper_a_r >> (defaultAlphabet as List<Character>)
        publicParameters.n_max >> 3
        publicParameters.identificationGroup >> identificationGroup
        identificationGroup.q_circ >> FIVE

        codeSheetPreparation = new CodeSheetPreparationAlgorithms(publicParameters)

    }

    def "getCodeSheets should combine the secret voter data in the expected manner"() {
        given:
        ElectionSet electionSet = Mock()
        Voter voter1 = Mock()
        Voter voter2 = Mock()
        Election election1 = Mock()
        Election election2 = Mock()
        Candidate candidate1 = Mock()
        Candidate candidate2 = Mock()
        Candidate candidate3 = Mock()
        Candidate candidate4 = Mock()
        SecretVoterData voter1Authority1 = Mock()
        SecretVoterData voter1Authority2 = Mock()
        SecretVoterData voter2Authority1 = Mock()
        SecretVoterData voter2Authority2 = Mock()

        electionSet.voters >> [voter1, voter2]
        electionSet.elections >> [election1, election2]
        electionSet.candidates >> [candidate1, candidate2, candidate3, candidate4]

        and: "both voters are eligible for the first election"
        electionSet.isEligible(_ as Voter, election1) >> true

        and: "only the first voter is eligible for the second election"
        electionSet.isEligible(voter1, election2) >> true
        electionSet.isEligible(voter2, election2) >> false

        and: "the first election is a 1-out-of-3 election (typical referendum)"
        election1.numberOfCandidates >> 2
        election1.numberOfSelections >> 1

        and: "the second election is a 2-out-of-4 election"
        election2.numberOfCandidates >> 2
        election2.numberOfSelections >> 1

        and: "the following values for voter 1"
        voter1Authority1.x >> ZERO
        voter1Authority2.x >> TWO
        voter1Authority1.y >> TWO
        voter1Authority2.y >> ONE
        voter1Authority1.f >> ([0x01, 0x01] as byte[])
        voter1Authority2.f >> ([0x11, 0x11] as byte[])
        voter1Authority1.rc >> ([[0x01, 0x02], [0x03, 0x04], [0x05, 0x06], [0x07, 0x08]] as byte[][])
        voter1Authority2.rc >> ([[0xC1, 0xD2], [0xE3, 0xF4], [0xC5, 0xD6], [0xE7, 0xF8]] as byte[][])

        and: "the following values for voter 2"
        voter2Authority1.x >> FOUR
        voter2Authority2.x >> ZERO
        voter2Authority1.y >> ONE
        voter2Authority2.y >> ONE
        voter2Authority1.f >> ([0x0F, 0x0F] as byte[])
        voter2Authority2.f >> ([0x31, 0x41] as byte[])
        voter2Authority1.rc >> ([[0x01, 0x02], [0x03, 0x04], [0x05, 0x06], [0x07, 0x08]] as byte[][])
        voter2Authority2.rc >> ([[0xC0, 0xD0], [0xE0, 0xF0], [0xC0, 0xD0], [0xE0, 0xF0]] as byte[][])

        when:
        def sheets = codeSheetPreparation.getSheets(
                electionSet,
                [
                        [voter1Authority1, voter2Authority1], // Authority 1; i.e. bold_d_1
                        [voter1Authority2, voter2Authority2] // Authority 2; i.e. bold_d_2
                ])

        then:
        def sheet_1 = sheets.get(0)
        sheet_1.voter == voter1
        sheet_1.electionSet == electionSet
        sheet_1.upper_x == "ac" // 0 + 2 = 2
        sheet_1.upper_y == "ad" // 2 + 1 = 3
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
        sheet_2.upper_x == "ae" // 4 + 0 = 4
        sheet_2.upper_y == "ac" // 1 + 1 = 2
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
