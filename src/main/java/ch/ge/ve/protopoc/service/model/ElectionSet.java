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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class containing the definition of a set of elections
 */
public class ElectionSet {
    private final List<Voter> voters;
    private final List<Candidate> candidates;
    private final List<Election> elections;

    public ElectionSet(List<Voter> voters, List<Candidate> candidates, List<Election> elections) {
        Preconditions.checkArgument(candidates.size() ==
                elections.stream().map(Election::getNumberOfCandidates).reduce((a, b) -> a + b).orElse(0));
        this.voters = voters;
        this.candidates = candidates;
        this.elections = elections;
    }

    public boolean isEligible(Voter voter, Election election) {
        return voter.getAllowedDomainsOfInfluence().contains(election.getApplicableDomainofInfluence());
    }

    public List<Integer> getBold_n() {
        return elections.stream().map(Election::getNumberOfCandidates).collect(Collectors.toList());
    }

    public List<Voter> getVoters() {
        return ImmutableList.copyOf(voters);
    }

    public List<Candidate> getCandidates() {
        return ImmutableList.copyOf(candidates);
    }

    public List<Election> getElections() {
        return ImmutableList.copyOf(elections);
    }
}
