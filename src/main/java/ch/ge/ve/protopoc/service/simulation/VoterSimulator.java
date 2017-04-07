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

import ch.ge.ve.protopoc.service.algorithm.VoteConfirmationVoterAlgorithms;
import ch.ge.ve.protopoc.service.exception.VoteCastingException;
import ch.ge.ve.protopoc.service.exception.VoteConfirmationException;
import ch.ge.ve.protopoc.service.model.CodeSheet;
import ch.ge.ve.protopoc.service.model.VotingPageData;
import ch.ge.ve.protopoc.service.protocol.VotingClientService;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Simulation class for a Voter, with interfaces to receive the code sheet, initiate a voting client, and perform
 * selections on elections
 */
public class VoterSimulator {
    private static final Logger log = LoggerFactory.getLogger(VoterSimulator.class);
    private final Integer voterIndex;
    private final VotingClientService votingClient;
    private final VoteConfirmationVoterAlgorithms voteConfirmationVoterAlgorithms;
    private final Random random = new Random(); // doesn't need to be secure, only used for simulation of user choices
    private CodeSheet codeSheet;

    public VoterSimulator(Integer voterIndex, VotingClientService votingClient,
                          VoteConfirmationVoterAlgorithms voteConfirmationVoterAlgorithms) {
        this.votingClient = votingClient;
        this.voterIndex = voterIndex;
        this.voteConfirmationVoterAlgorithms = voteConfirmationVoterAlgorithms;
    }

    public void sendCodeSheet(CodeSheet codeSheet) {
        Preconditions.checkState(this.codeSheet == null,
                String.format("The code sheet may not be updated once set (at voter %d)", voterIndex));
        Preconditions.checkArgument(codeSheet.getI() == voterIndex, "Voter received the wrong code list.é");
        this.codeSheet = codeSheet;
    }

    public List<Integer> vote() {
        Preconditions.checkState(codeSheet != null,
                "The voter needs their code sheet to vote");

        log.info(String.format("Voter %d starting vote", voterIndex));
        VotingPageData votingPageData = votingClient.startVoteSession(voterIndex);

        List<Integer> selections = pickAtRandom(
                votingPageData.getSelectionCounts(),
                votingPageData.getCandidateCounts());
        log.info(String.format("Voter %d selections: %s", voterIndex, selections));

        List<String> verificationCodes;
        try {
            log.info(String.format("Voter %d submitting vote", voterIndex));
            verificationCodes = votingClient.sumbitVote(codeSheet.getUpper_x(), selections);
        } catch (VoteCastingException e) {
            log.error(String.format("Voter %d: error during vote casting", voterIndex), e);
            throw new VoteProcessException(e);
        }

        log.info(String.format("Voter %d checking verification codes", voterIndex));
        if (!voteConfirmationVoterAlgorithms.checkReturnCodes(codeSheet.getBold_rc(), verificationCodes, selections)) {
            throw new VoteProcessException(new VerificationCodesNotMatchingException("Verification codes do not match"));
        }

        String finalizationCode;
        try {
            log.info(String.format("Voter %d confirming vote", voterIndex));
            finalizationCode = votingClient.confirmVote(codeSheet.getUpper_y());
        } catch (VoteConfirmationException e) {
            log.error(String.format("Voter %d: error during vote confirmation", voterIndex), e);
            throw new VoteProcessException(e);
        }

        log.info(String.format("Voter %d checking finalization code", voterIndex));
        if (!voteConfirmationVoterAlgorithms.checkFinalizationCode(codeSheet.getUpper_fc(), finalizationCode)) {
            throw new VoteProcessException(new FinalizationCodeNotMatchingException("Finalization code does not match"));
        }

        log.info(String.format("Voter %d done voting", voterIndex));

        return selections;
    }

    private List<Integer> pickAtRandom(List<Integer> selectionCounts, List<Integer> candidateCounts) {
        Preconditions.checkArgument(selectionCounts.size() == candidateCounts.size(),
                "The size of both lists should be identical");

        List<Integer> selections = new ArrayList<>();
        int n_offset = 1;

        for (int i = 0; i < selectionCounts.size(); i++) {
            int numberOfSelections = selectionCounts.get(i);
            int numberOfCandidates = candidateCounts.get(i);


            for (int j = 0; j < numberOfSelections; j++) {
                Integer s_j;
                do {
                    s_j = random.nextInt(numberOfCandidates) + n_offset;
                } while (selections.contains(s_j));
                selections.add(s_j);
            }

            n_offset += numberOfCandidates;
        }

        return selections.stream().sorted().collect(Collectors.toList());
    }

    public VotingClientService getVotingClient() {
        return votingClient;
    }
}
