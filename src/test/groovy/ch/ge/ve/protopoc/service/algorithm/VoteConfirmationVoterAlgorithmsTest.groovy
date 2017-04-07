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

import spock.lang.Specification

/**
 * Tests on the vote confirmation algorithms performed by the voter
 */
class VoteConfirmationVoterAlgorithmsTest extends Specification {
    VoteConfirmationVoterAlgorithms voteConfirmationVoter

    void setup() {
        voteConfirmationVoter = new VoteConfirmationVoterAlgorithms()
    }

    def "checkReturnCodes should verify return codes"() {
        expect:
        result == voteConfirmationVoter.checkReturnCodes(bold_rc, bold_rc_prime, bold_s)

        where:
        bold_rc              | bold_rc_prime | bold_s || result
        ["a", "b", "c", "d"] | ["c"]         | [3]    || true
        ["a", "b", "c", "d"] | ["b", "d"]    | [2, 4] || true
        ["ab", "cd"]         | ["ab"]        | [1]    || true
        ["a", "b", "c", "d"] | ["d"]         | [3]    || false
        ["a", "b", "c", "d"] | ["b", "c"]    | [2, 4] || false
        ["ab", "cd"]         | ["cd"]        | [1]    || false
    }

    def "checkFinalizationCode should verify the finalization code"() {
        expect:
        result == voteConfirmationVoter.checkFinalizationCode(F, F_prime)

        where:
        F    | F_prime || result
        "a"  | "a"     || true
        "ab" | "ab"    || true
        "a"  | "ab"    || false
        "ab" | "a"     || false
        "ab" | "AB"    || false
        "Ab" | "ab"    || false

    }
}
