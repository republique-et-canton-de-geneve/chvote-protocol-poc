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

package ch.ge.ve.protopoc.service.simulation;

import ch.ge.ve.protopoc.service.algorithm.VotingCardPreparationAlgorithms;
import ch.ge.ve.protopoc.service.model.ElectionSet;
import ch.ge.ve.protopoc.service.model.PublicParameters;
import ch.ge.ve.protopoc.service.model.SecretVoterData;
import ch.ge.ve.protopoc.service.model.VotingCard;
import ch.ge.ve.protopoc.service.protocol.AuthorityService;
import ch.ge.ve.protopoc.service.protocol.BulletinBoardService;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulation class for the Printing Authority
 */
public class PrintingAuthoritySimulator {
    private final BulletinBoardService bulletinBoardService;
    private final List<AuthorityService> authorities = new ArrayList<>();
    private final List<VoterSimulator> voterSimulators = new ArrayList<>();
    private final VotingCardPreparationAlgorithms votingCardPreparationAlgorithms;

    public PrintingAuthoritySimulator(BulletinBoardService bulletinBoardService, VotingCardPreparationAlgorithms votingCardPreparationAlgorithms) {
        this.bulletinBoardService = bulletinBoardService;
        this.votingCardPreparationAlgorithms = votingCardPreparationAlgorithms;
    }

    public void setAuthorities(List<AuthorityService> authorities) {
        Preconditions.checkState(this.authorities.isEmpty(),
                "The authorities cannot be changed once they have been set");
        this.authorities.addAll(authorities);
    }

    public void setVoterSimulators(List<VoterSimulator> voterSimulators) {
        Preconditions.checkState(this.voterSimulators.isEmpty(),
                "The voter simulators may not be updated once set");
        this.voterSimulators.addAll(voterSimulators);
    }

    public void print() {
        PublicParameters publicParameters = bulletinBoardService.getPublicParameters();
        Preconditions.checkState(authorities.size() == publicParameters.getS(),
                "The number of authorities should match the public parameters");
        ElectionSet electionSet = bulletinBoardService.getElectionSet();
        Preconditions.checkState(voterSimulators.size() == electionSet.getVoters().size(),
                "The number of voter simulators should be equal to " +
                        "the number of voters in the election set");

        List<List<SecretVoterData>> voterDataMatrix = new ArrayList<>();
        for (AuthorityService authority : authorities) {
            voterDataMatrix.add(authority.getPrivateCredentials());
        }

        List<VotingCard> sheets = votingCardPreparationAlgorithms.getVotingCard(electionSet, voterDataMatrix);

        for (int i = 0; i < sheets.size(); i++) {
            voterSimulators.get(i).sendCodeSheet(sheets.get(i));
        }
    }
}
