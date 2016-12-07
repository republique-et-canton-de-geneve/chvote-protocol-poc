package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.exception.VoteCastingException;
import ch.ge.ve.protopoc.service.exception.VoteConfirmationException;
import ch.ge.ve.protopoc.service.model.VotingPageData;

import java.util.List;

/**
 * This interface defines the contract for the voting client
 */
public interface VotingClientService {
    VotingPageData startVoteSession(Integer voterIndex);

    List<String> sumbitVote(String identificationCredentials, List<Integer> selections) throws VoteCastingException;

    String confirmVote(String confirmationCredentials) throws VoteConfirmationException;
}
