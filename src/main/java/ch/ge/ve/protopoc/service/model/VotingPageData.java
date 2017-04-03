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

package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Model class containing the data necessary for presenting the voting page to the voter
 */
public class VotingPageData {
    private final List<Integer> selectionCounts;
    private final List<Integer> candidateCounts;

    public VotingPageData(List<Integer> selectionCounts, List<Integer> candidateCounts) {
        this.selectionCounts = selectionCounts;
        this.candidateCounts = candidateCounts;
    }

    public List<Integer> getSelectionCounts() {
        return selectionCounts;
    }

    public List<Integer> getCandidateCounts() {
        return candidateCounts;
    }
}
