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

package ch.ge.ve.protopoc.service.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Contains all the information needed for the printing of a code sheet
 */
public final class CodeSheet {
    private final Integer i;
    private final Voter voter;
    private final ElectionSet electionSet;
    private final List<Integer> bold_k;
    private final String upper_x;
    private final String upper_y;
    private final String upper_fc;
    private final List<String> bold_rc;

    public CodeSheet(Integer i, Voter upper_v, ElectionSet electionSet, List<Integer> bold_k, String upper_x, String upper_y, String
            upper_fc, List<String> bold_rc) {
        this.i = i;
        voter = upper_v;
        this.electionSet = electionSet;
        this.bold_k = ImmutableList.copyOf(bold_k);
        this.upper_x = upper_x;
        this.upper_y = upper_y;
        this.upper_fc = upper_fc;
        this.bold_rc = bold_rc;
    }

    public Integer getI() {
        return i;
    }

    public Voter getVoter() {
        return voter;
    }

    public ElectionSet getElectionSet() {
        return electionSet;
    }

    public List<Integer> getBold_k() {
        return ImmutableList.copyOf(bold_k);
    }

    public String getUpper_x() {
        return upper_x;
    }

    public String getUpper_y() {
        return upper_y;
    }

    public String getUpper_fc() {
        return upper_fc;
    }

    public List<String> getBold_rc() {
        return bold_rc;
    }
}
