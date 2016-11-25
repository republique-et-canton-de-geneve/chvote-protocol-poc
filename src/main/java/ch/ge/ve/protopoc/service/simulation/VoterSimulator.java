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
        this.codeSheet = codeSheet;
    }

    public void vote() {
        Preconditions.checkState(codeSheet != null,
                "The voter needs their code sheet to vote");

        log.info(String.format("Voter %d starting vote", voterIndex));
        VotingPageData votingPageData = votingClient.startVoteSession(voterIndex);

        List<Integer> selections = pickAtRandom(
                votingPageData.getSelectionCounts(),
                votingPageData.getCandidateCounts());

        byte[][] returnCodes;
        try {
            log.info(String.format("Voter %d submitting vote", voterIndex));
            returnCodes = votingClient.sumbitVote(codeSheet.getX_i(), selections);
        } catch (VoteCastingException e) {
            log.error(String.format("Voter %d: error during vote casting", voterIndex), e);
            throw new VoteProcessException(e);
        }

        log.info(String.format("Voter %d checking return codes", voterIndex));
        if (!voteConfirmationVoterAlgorithms.checkReturnCodes(codeSheet.getRc_i(), returnCodes, selections)) {
            throw new VoteProcessException(new ReturnCodesNotMatchingException("Return codes do not match"));
        }

        byte[] finalizationCode;
        try {
            log.info(String.format("Voter %d confirming vote", voterIndex));
            finalizationCode = votingClient.confirmVote(codeSheet.getY_i());
        } catch (VoteConfirmationException e) {
            log.error(String.format("Voter %d: error during vote confirmation", voterIndex), e);
            throw new VoteProcessException(e);
        }

        log.info(String.format("Voter %d checking finalizaztion code", voterIndex));
        if (!voteConfirmationVoterAlgorithms.checkFinalizationCode(codeSheet.getF_i(), finalizationCode)) {
            throw new VoteProcessException(new FinalizationCodeNotMatchingException("Finalization code does not match"));
        }

        log.info(String.format("Voter %d done voting", voterIndex));
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

        return selections;
    }
}
