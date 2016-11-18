package ch.ge.ve.protopoc.service.algorithm

import spock.lang.Specification

/**
 * Missing javadoc!
 */
class VoteConfirmationVoterTest extends Specification {
    def VoteConfirmationVoter voteConfirmationVoter

    void setup() {
        voteConfirmationVoter = new VoteConfirmationVoter()
    }

    def "checkReturnCodes should verify return codes"() {
        expect:
        result == voteConfirmationVoter.checkReturnCodes(bold_rc as byte[][], bold_rc_prime as byte[][], bold_s)

        where:
        bold_rc                          | bold_rc_prime    | bold_s || result
        [[0x00], [0x01], [0x02], [0x03]] | [[0x02]]         | [2]    || true
        [[0x00], [0x01], [0x02], [0x03]] | [[0x01], [0x03]] | [1, 3] || true
        [[0x00, 0x01], [0x02, 0x03]]     | [[0x00, 0x01]]   | [0]    || true
        [[0x00], [0x01], [0x02], [0x03]] | [[0x03]]         | [2]    || false
        [[0x00], [0x01], [0x02], [0x03]] | [[0x02], [0x03]] | [1, 3] || false
        [[0x00, 0x01], [0x02, 0x03]]     | [[0x02, 0x03]]   | [0]    || false
    }

    def "checkFinalizationCode should verify the finalization code"() {
        expect:
        result == voteConfirmationVoter.checkFinalizationCode(F as byte[], F_prime as byte[])

        where:
        F            | F_prime      || result
        [0x01]       | [0x01]       || true
        [0x01, 0x02] | [0x01, 0x02] || true
        [0x01]       | [0x01, 0x02] || false
        [0x01, 0x02] | [0x01]       || false
        [0x01, 0x02] | [0xF1, 0xF2] || false
        [0xF1, 0x02] | [0x01, 0x02] || false

    }
}
