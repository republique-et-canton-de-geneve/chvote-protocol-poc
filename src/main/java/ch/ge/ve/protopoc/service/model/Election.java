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

/**
 * An election has a number of candidates and a number of allowed selections.
 * It is applicable to a domain of influence.
 */
public final class Election {
    private final int numberOfCandidates;
    private final int numberOfSelections;
    private final DomainOfInfluence applicableDomainofInfluence;

    public Election(int numberOfCandidates, int numberOfSelections, DomainOfInfluence applicableDomainofInfluence) {
        this.numberOfCandidates = numberOfCandidates;
        this.numberOfSelections = numberOfSelections;
        this.applicableDomainofInfluence = applicableDomainofInfluence;
    }

    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    public int getNumberOfSelections() {
        return numberOfSelections;
    }

    public DomainOfInfluence getApplicableDomainofInfluence() {
        return applicableDomainofInfluence;
    }
}
