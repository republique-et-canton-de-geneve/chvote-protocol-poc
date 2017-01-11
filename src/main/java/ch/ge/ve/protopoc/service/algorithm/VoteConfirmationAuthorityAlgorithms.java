package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.BallotNotFoundException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import ch.ge.ve.protopoc.service.support.ByteArrayUtils;
import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;

/**
 * Algorithms for the vote confirmation phase, on the authorities side
 */
public class VoteConfirmationAuthorityAlgorithms {
    private final PublicParameters publicParameters;
    private final GeneralAlgorithms generalAlgorithms;
    private final VoteCastingAuthorityAlgorithms voteCastingAuthorityAlgorithms;
    private final Hash hash;

    public VoteConfirmationAuthorityAlgorithms(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms,
                                               VoteCastingAuthorityAlgorithms voteCastingAuthorityAlgorithms, Hash hash) {
        this.publicParameters = publicParameters;
        this.generalAlgorithms = generalAlgorithms;
        this.voteCastingAuthorityAlgorithms = voteCastingAuthorityAlgorithms;
        this.hash = hash;
    }

    /**
     * Algorithm 7.36: CheckConfirmation
     *
     * @param i           the voter index
     * @param gamma       the voter's confirmation, including public confirmation credential and proof of knowledge of
     *                    the private confirmation credential
     * @param bold_y_circ the list of public confirmation credentials, as generated during the preparation phase
     * @param B           the current list of ballots
     * @param C           the current list of confirmations
     * @return true if the confirmation is allowed (ballot present, confirmation not present, credentials match) and the
     * proof is valid
     */
    public boolean checkConfirmation(Integer i, Confirmation gamma, List<BigInteger> bold_y_circ,
                                     Collection<BallotEntry> B, Collection<ConfirmationEntry> C) {
        return voteCastingAuthorityAlgorithms.hasBallot(i, B) &&
                !hasConfirmation(i, C) &&
                bold_y_circ.get(i).compareTo(gamma.getY_circ()) == 0 &&
                checkConfirmationProof(gamma.getPi(), gamma.getY_circ());
    }

    /**
     * Algorithm 7.37: HasConfirmation
     *
     * @param i the voter index
     * @param C the list of confirmations
     * @return true if the list of confirmation contains a confirmation for the given voter index, false otherwise
     */
    public boolean hasConfirmation(Integer i, Collection<ConfirmationEntry> C) {
        return C.stream().anyMatch(c -> c.getI().equals(i));
    }

    /**
     * Algorithm 7.38: CheckConfirmationProof
     *
     * @param pi     the proof of knowledge of private confirmation credential y, provided by the voting client
     * @param y_circ the public confirmation credential corresponding to the private credential y
     * @return true if the proof of knowledge is valid, false otherwise
     */
    public boolean checkConfirmationProof(NonInteractiveZKP pi, BigInteger y_circ) {
        Preconditions.checkNotNull(pi);
        Preconditions.checkNotNull(pi.getT());
        Preconditions.checkNotNull(pi.getS());
        Preconditions.checkNotNull(y_circ);
        Preconditions.checkArgument(pi.getT().size() == 1);
        Preconditions.checkArgument(pi.getS().size() == 1);

        BigInteger p_circ = publicParameters.getIdentificationGroup().getP_circ();
        BigInteger q_circ = publicParameters.getIdentificationGroup().getQ_circ();
        BigInteger g_circ = publicParameters.getIdentificationGroup().getG_circ();

        BigInteger t = pi.getT().get(0);
        BigInteger s = pi.getS().get(0);

        BigInteger c = generalAlgorithms.getNIZKPChallenge(new BigInteger[]{y_circ}, new BigInteger[]{t}, q_circ);
        BigInteger t_prime = modExp(g_circ, s, p_circ).multiply(modExp(y_circ, c.negate(), p_circ)).mod(p_circ);

        return t.compareTo(t_prime) == 0;
    }

    /**
     * Algorithm 7.39: GetFinalization
     *
     * @param i      the voter index
     * @param bold_P the point matrix, one point per voter per candidate
     * @param B      the current ballot list
     * @return this authority's part of the finalization code
     */
    public FinalizationCodePart getFinalization(Integer i, List<List<Point>> bold_P, Collection<BallotEntry> B) {
        Object[] bold_p_i = bold_P.get(i).toArray();
        byte[] F = ByteArrayUtils.truncate(hash.recHash_L(bold_p_i), publicParameters.getL_f() / 8);

        BallotEntry ballotEntry = B.stream().filter(b -> Objects.equals(b.getI(), i)).findFirst().orElseThrow(
                () -> new BallotNotFoundException(String.format("Couldn't find any ballot for voter %d", i))
        );

        return new FinalizationCodePart(F, ballotEntry.getBold_r());
    }
}
