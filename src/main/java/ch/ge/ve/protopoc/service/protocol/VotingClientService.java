package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.VoteCastingException;
import ch.ge.ve.protopoc.service.exception.VoteConfirmationException;
import ch.ge.ve.protopoc.service.model.VotingPageData;

import java.util.List;

/**
 * This interface defines the contract for the voting client
 */
public interface VotingClientService {
    VotingPageData startVoteSession(Integer voterIndex);

    byte[][] sumbitVote(byte[] identificationCredentials, List<Integer> selections) throws IncompatibleParametersException, VoteCastingException;

    byte[] confirmVote(byte[] confirmationCredentials) throws VoteConfirmationException;
}
