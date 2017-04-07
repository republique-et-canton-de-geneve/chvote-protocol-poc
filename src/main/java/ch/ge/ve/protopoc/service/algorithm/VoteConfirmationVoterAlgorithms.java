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

package ch.ge.ve.protopoc.service.algorithm;

import java.util.List;

/**
 * Algorithms for the vote confirmation phase, on the voter's end.
 * <p>
 * These will be performed by humans in the real system, and are only implemented to facilitate complete simulations.
 */
public class VoteConfirmationVoterAlgorithms {
    /**
     * Algorithm 7.29: CheckReturnCodes
     *
     * @param bold_rc       the printed return codes, received before the vote casting phase
     * @param bold_rc_prime the displayed return codes, shown during the vote casting session
     * @param bold_s        the voter's selections
     * @return true if every displayed return code matches the corresponding printed return code
     */
    public boolean checkReturnCodes(List<String> bold_rc, List<String> bold_rc_prime, List<Integer> bold_s) {
        for (int i = 0; i < bold_s.size(); i++) {
            // selections are 1-based
            if (!bold_rc.get(bold_s.get(i) - 1).equals(bold_rc_prime.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Algorithm 7.39: CheckFinalizationCode
     *
     * @param upper_fc       the printed finalization code, received before the vote casting phase
     * @param upper_fc_prime the displayed finalization code, shown during the vote casting session
     * @return true if both finalization codes match, false otherwise
     */
    public boolean checkFinalizationCode(String upper_fc, String upper_fc_prime) {
        return upper_fc.equals(upper_fc_prime);
    }
}
