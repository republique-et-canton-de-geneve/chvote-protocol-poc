package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.BallotEntry;
import ch.ge.ve.protopoc.service.model.ConfirmationEntry;
import ch.ge.ve.protopoc.service.model.Encryption;
import ch.ge.ve.protopoc.service.model.PublicParameters;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Algorithms performed during the mixing phase, by the autorities
 */
public class MixingAuthorityAlgorithms {
    private final PublicParameters publicParameters;
    private final VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms;

    public MixingAuthorityAlgorithms(PublicParameters publicParameters, VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms) {
        this.publicParameters = publicParameters;
        this.voteConfirmationAuthorityAlgorithms = voteConfirmationAuthorityAlgorithms;
    }

    /**
     * Algorithm 5.40: GetEncryptions
     *
     * @param ballotList       the list of ballots submitted to the bulletin board
     * @param confirmationList the list of confirmations submitted to the bulletin board
     * @return the list of the encryptions for the valid, confirmed ballots
     */
    public List<Encryption> getEncryptions(List<BallotEntry> ballotList, List<ConfirmationEntry> confirmationList) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();

        return ballotList.stream()
                .filter(B -> voteConfirmationAuthorityAlgorithms.hasConfirmation(B.getI(), confirmationList))
                .map(B -> {
                    BigInteger a_j = B.getAlpha().getBold_a().stream()
                            .reduce(BigInteger::multiply)
                            .orElseThrow(() -> new IllegalArgumentException("can't happen if protocol was followed"))
                            .mod(p);
                    return new Encryption(a_j, B.getAlpha().getB());
                })
                .collect(Collectors.toList());
    }
}
