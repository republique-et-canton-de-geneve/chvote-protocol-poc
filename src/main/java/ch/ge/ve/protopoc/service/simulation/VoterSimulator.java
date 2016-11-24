package ch.ge.ve.protopoc.service.simulation;

import ch.ge.ve.protopoc.service.algorithm.VoteConfirmationVoterAlgorithms;
import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.VoteCastingException;
import ch.ge.ve.protopoc.service.exception.VoteConfirmationException;
import ch.ge.ve.protopoc.service.model.CodeSheet;
import ch.ge.ve.protopoc.service.model.VotingPageData;
import ch.ge.ve.protopoc.service.protocol.VotingClientService;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulation class for a Voter, with interfaces to receive the code sheet, initiate a voting client, and perform
 * selections on elections
 */
public class VoterSimulator {
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
        Preconditions.checkState(codeSheet == null,
                "The code sheet may not be updated once set");
        this.codeSheet = codeSheet;
    }

    public void vote() throws IncompatibleParametersException, VoteCastingException, ReturnCodesNotMatchingException,
            VoteConfirmationException, FinalizationCodeNotMatchingException {
        Preconditions.checkState(codeSheet != null,
                "The voter needs their code sheet to vote");

        VotingPageData votingPageData = votingClient.startVoteSession(voterIndex);

        List<Integer> selections = pickAtRandom(
                votingPageData.getSelectionCounts(),
                votingPageData.getCandidateCounts());

        byte[][] returnCodes = votingClient.sumbitVote(codeSheet.getX_i(), selections);

        if (!voteConfirmationVoterAlgorithms.checkReturnCodes(codeSheet.getRc_i(), returnCodes, selections)) {
            throw new ReturnCodesNotMatchingException("Return codes do not match");
        }

        byte[] finalizationCode = votingClient.confirmVote(codeSheet.getY_i());

        if (!voteConfirmationVoterAlgorithms.checkFinalizationCode(codeSheet.getF_i(), finalizationCode)) {
            throw new FinalizationCodeNotMatchingException("Finalization code does not match");
        }
    }

    private List<Integer> pickAtRandom(List<Integer> selectionCounts, List<Integer> candidateCounts) {
        Preconditions.checkArgument(selectionCounts.size() == candidateCounts.size(),
                "The size of both lists should be identical");

        List<Integer> selections = new ArrayList<>();
        int n_offset = 0;

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

        return selections;
    }
}
