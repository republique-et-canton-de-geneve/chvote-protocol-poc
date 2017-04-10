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

package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.BallotNotFoundRuntimeException;
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
     * Algorithm 7.34: CheckConfirmation
     *
     * @param i          the voter index
     * @param gamma      the voter's confirmation, including public confirmation credential and proof of knowledge of
     *                   the private confirmation credential
     * @param bold_y_hat the list of public confirmation credentials, as generated during the preparation phase
     * @param upper_b    the current list of ballots
     * @param upper_c    the current list of confirmations
     * @return true if the confirmation is allowed (ballot present, confirmation not present, credentials match) and the
     * proof is valid
     */
    public boolean checkConfirmation(Integer i, Confirmation gamma, List<BigInteger> bold_y_hat,
                                     Collection<BallotEntry> upper_b, Collection<ConfirmationEntry> upper_c) {
        return voteCastingAuthorityAlgorithms.hasBallot(i, upper_b) &&
                !hasConfirmation(i, upper_c) &&
                bold_y_hat.get(i).compareTo(gamma.getY_hat()) == 0 &&
                checkConfirmationProof(gamma.getPi(), gamma.getY_hat());
    }

    /**
     * Algorithm 7.35: HasConfirmation
     *
     * @param i       the voter index
     * @param upper_c the list of confirmations
     * @return true if the list of confirmation contains a confirmation for the given voter index, false otherwise
     */
    public boolean hasConfirmation(Integer i, Collection<ConfirmationEntry> upper_c) {
        return upper_c.stream().anyMatch(c -> c.getI().equals(i));
    }

    /**
     * Algorithm 7.36: CheckConfirmationProof
     *
     * @param pi    the proof of knowledge of private confirmation credential y, provided by the voting client
     * @param y_hat the public confirmation credential corresponding to the private credential y
     * @return true if the proof of knowledge is valid, false otherwise
     */
    public boolean checkConfirmationProof(NonInteractiveZKP pi, BigInteger y_hat) {
        Preconditions.checkNotNull(pi);
        Preconditions.checkNotNull(pi.getT());
        Preconditions.checkNotNull(pi.getS());
        Preconditions.checkNotNull(y_hat);
        Preconditions.checkArgument(pi.getT().size() == 1);
        Preconditions.checkArgument(pi.getS().size() == 1);

        BigInteger p_hat = publicParameters.getIdentificationGroup().getP_hat();
        BigInteger q_hat = publicParameters.getIdentificationGroup().getQ_hat();
        BigInteger g_hat = publicParameters.getIdentificationGroup().getG_hat();

        BigInteger t = pi.getT().get(0);
        BigInteger s = pi.getS().get(0);

        Preconditions.checkArgument(generalAlgorithms.isMember_G_q_hat(t),
                "t must be in G_q_hat");
        Preconditions.checkArgument(generalAlgorithms.isInZ_q_hat(s),
                "s must be in Z_q_hat");
        //noinspection SuspiciousNameCombination
        Preconditions.checkArgument(generalAlgorithms.isMember_G_q_hat(y_hat),
                "y_hat must be in G_q_hat");

        BigInteger c = generalAlgorithms.getNIZKPChallenge(new BigInteger[]{y_hat}, new BigInteger[]{t}, q_hat);
        BigInteger t_prime = modExp(g_hat, s, p_hat).multiply(modExp(y_hat, c.negate(), p_hat)).mod(p_hat);

        return t.compareTo(t_prime) == 0;
    }

    /**
     * Algorithm 7.37: GetFinalization
     *
     * @param i            the voter index
     * @param upper_bold_p the point matrix, one point per voter per candidate
     * @param upper_b      the current ballot list
     * @return this authority's part of the finalization code
     */
    public FinalizationCodePart getFinalization(Integer i, List<List<Point>> upper_bold_p, Collection<BallotEntry> upper_b) {
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        Preconditions.checkArgument(upper_bold_p.stream().flatMap(Collection::stream)
                        .allMatch(point -> BigInteger.ZERO.compareTo(point.x) <= 0 &&
                                point.x.compareTo(p_prime) < 0 &&
                                BigInteger.ZERO.compareTo(point.y) <= 0 &&
                                point.y.compareTo(p_prime) < 0),
                "All points' coordinates must be in Z_p_prime");
        Preconditions.checkElementIndex(i, upper_bold_p.size());

        Object[] bold_p_i = upper_bold_p.get(i).toArray();
        byte[] upper_f_i = ByteArrayUtils.truncate(hash.recHash_L(bold_p_i), publicParameters.getUpper_l_f());

        BallotEntry ballotEntry = upper_b.stream().filter(b -> Objects.equals(b.getI(), i)).findFirst().orElseThrow(
                () -> new BallotNotFoundRuntimeException(String.format("Couldn't find any ballot for voter %d", i))
        );

        return new FinalizationCodePart(upper_f_i, ballotEntry.getBold_r());
    }
}
