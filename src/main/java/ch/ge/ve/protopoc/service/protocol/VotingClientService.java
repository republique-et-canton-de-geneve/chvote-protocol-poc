package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.model.VotingPageData;

import java.math.BigInteger;
import java.util.List;

/**
 * This interface defines the contract for the voting client
 */
public interface VotingClientService {
    VotingPageData startVoteSession(Integer voterIndex);

    byte[][] sumbitVote(byte[] identificationCredentials, List<BigInteger> selections);

    byte[] confirmVote(byte[] confirmationCredentials);
}
