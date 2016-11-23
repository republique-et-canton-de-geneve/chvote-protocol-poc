package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.*
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE

/**
 * Tests for the algorithms related to the code sheets preparation
 */
class CodeSheetPreparationAlgorithmsTest extends Specification {
    PublicParameters publicParameters = Mock()

    CodeSheetPreparationAlgorithms codeSheetPreparation

    void setup() {
        publicParameters.s >> 2 // For the sake of simplifying the examples
        publicParameters.l_x >> 16
        publicParameters.l_y >> 16
        publicParameters.l_f >> 16
        publicParameters.l_r >> 16

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
        voter1Authority1.x >> FIVE
        voter1Authority2.x >> THREE
        voter1Authority1.y >> TWO
        voter1Authority2.y >> ONE
        voter1Authority1.f >> ([0x01, 0x01] as byte[])
        voter1Authority2.f >> ([0x11, 0x11] as byte[])
        voter1Authority1.rc >> ([[0x01, 0x02], [0x03, 0x04], [0x05, 0x06], [0x07, 0x08]] as byte[][])
        voter1Authority2.rc >> ([[0xC1, 0xD2], [0xE3, 0xF4], [0xC5, 0xD6], [0xE7, 0xF8]] as byte[][])

        and: "the following values for voter 2"
        voter2Authority1.x >> FOUR
        voter2Authority2.x >> ONE
        voter2Authority1.y >> THREE
        voter2Authority2.y >> FOUR
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
        sheet_1.x_i == ([0x00, 0x08] as byte[])
        sheet_1.y_i == ([0x00, 0x03] as byte[])
        sheet_1.f_i == ([0x10, 0x10] as byte[])
        sheet_1.rc_i == ([[0xC0, 0xD0], [0xE0, 0xF0], [0xC0, 0xD0], [0xE0, 0xF0]] as byte[][])
        sheet_1.k_i == [1, 1]

        def sheet_2 = sheets.get(1)
        sheet_2.voter == voter2
        sheet_2.electionSet == electionSet
        sheet_2.x_i == ([0x00, 0x05] as byte[])
        sheet_2.y_i == ([0x00, 0x07] as byte[])
        sheet_2.f_i == ([0x3E, 0x4E] as byte[])
        sheet_2.rc_i == ([[0xC1, 0xD2], [0xE3, 0xF4], [0xC5, 0xD6], [0xE7, 0xF8]] as byte[][])
        sheet_2.k_i == [1, 0]
    }
}
