/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - protocol-poc-back                                                                              -
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

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import ch.ge.ve.protopoc.service.support.ByteArrayUtils;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;
import static java.math.BigInteger.ONE;

/**
 * Algorithms related to the vote casting phase, performed by the authorities
 */
public class VoteCastingAuthorityAlgorithms {
    private static final Logger log = LoggerFactory.getLogger(VoteConfirmationAuthorityAlgorithms.class);
    private final PublicParameters publicParameters;
    private final ElectionSet electionSet;
    private final GeneralAlgorithms generalAlgorithms;
    private final RandomGenerator randomGenerator;
    private final Hash hash;
    private final Conversion conversion = new Conversion();

    public VoteCastingAuthorityAlgorithms(PublicParameters publicParameters, ElectionSet electionSet,
                                          GeneralAlgorithms generalAlgorithms, RandomGenerator randomGenerator,
                                          Hash hash) {
        this.publicParameters = publicParameters;
        this.electionSet = electionSet;
        this.generalAlgorithms = generalAlgorithms;
        this.randomGenerator = randomGenerator;
        this.hash = hash;
    }

    /**
     * Algorithm 7.22: CheckBallot
     *
     * @param i          the voter index
     * @param alpha      the submitted ballot, including the oblivious transfer query
     * @param pk         the encryption public key
     * @param bold_x_hat the vector of public voter credentials
     * @param upper_b    the current ballot list
     * @return true if the ballot was valid
     */
    public boolean checkBallot(Integer i, BallotAndQuery alpha, EncryptionPublicKey pk,
                               List<BigInteger> bold_x_hat, Collection<BallotEntry> upper_b) {
        Preconditions.checkNotNull(i);
        Preconditions.checkNotNull(alpha);
        List<BigInteger> bold_a = alpha.getBold_a();
        Preconditions.checkNotNull(bold_a);
        Preconditions.checkArgument(bold_a.stream().allMatch(generalAlgorithms::isMember),
                "All of the a_j's must be members of G_q");
        Preconditions.checkArgument(generalAlgorithms.isMember(alpha.getB()),
                "b must be a member of G_q");

        int numberOfSelections = bold_a.size();
        Preconditions.checkArgument(numberOfSelections > 0);
        Voter voter = electionSet.getVoters().get(i);
        int k_i = electionSet.getElections().stream().filter(e -> electionSet.isEligible(voter, e))
                .mapToInt(Election::getNumberOfSelections).sum();
        Preconditions.checkArgument(numberOfSelections == k_i,
                "A voter may not submit more than his allowed number of selections");
        Preconditions.checkNotNull(pk);
        Preconditions.checkNotNull(bold_x_hat);
        Preconditions.checkElementIndex(i, bold_x_hat.size());
        Preconditions.checkNotNull(upper_b);

        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger x_hat_i = bold_x_hat.get(i);
        if (!hasBallot(i, upper_b) && alpha.getX_hat().compareTo(x_hat_i) == 0) {
            BigInteger a = bold_a.stream().reduce(BigInteger::multiply)
                    .orElse(ONE)
                    .mod(p);
            return checkBallotProof(alpha.getPi(), alpha.getX_hat(), a, alpha.getB(), pk);
        }
        return false;
    }

    /**
     * Algorithm 7.23: HasBallot
     *
     * @param i the voter index
     * @param B the current ballot list
     * @return true if any ballot in the list matches the given voter index, false otherwise
     */
    public boolean hasBallot(Integer i, Collection<BallotEntry> B) {
        Preconditions.checkNotNull(i);
        Preconditions.checkNotNull(B);

        return B.stream().anyMatch(b_j -> b_j.getI().equals(i));
    }

    /**
     * Algorithm 7.24: CheckBallotProof
     *
     * @param pi    the proof
     * @param x_hat public voting credential
     * @param a     first part of ElGamal encryption
     * @param b     second part of ElGamal encryption
     * @param pk    the encryption public key
     * @return true if the proof is valid, false otherwise
     */
    public boolean checkBallotProof(NonInteractiveZKP pi, BigInteger x_hat, BigInteger a, BigInteger b,
                                    EncryptionPublicKey pk) {
        Preconditions.checkNotNull(pi);
        List<BigInteger> t = pi.getT();
        Preconditions.checkNotNull(t);
        List<BigInteger> s = pi.getS();
        Preconditions.checkNotNull(s);
        Preconditions.checkNotNull(x_hat);
        Preconditions.checkNotNull(a);
        Preconditions.checkNotNull(b);
        Preconditions.checkNotNull(pk);
        Preconditions.checkNotNull(pk.getPublicKey());
        Preconditions.checkArgument(t.size() == 3, "t contains three elements");
        Preconditions.checkArgument(generalAlgorithms.isMember_G_q_hat(t.get(0)),
                "t_1 must be in G_q_hat");
        Preconditions.checkArgument(generalAlgorithms.isMember(t.get(1)),
                "t_2 must be in G_q");
        Preconditions.checkArgument(generalAlgorithms.isMember(t.get(2)),
                "t_3 must be in G_q");

        Preconditions.checkArgument(s.size() == 3, "s contains three elements");
        BigInteger s_1 = s.get(0);
        BigInteger s_2 = s.get(1);
        BigInteger s_3 = s.get(2);
        Preconditions.checkArgument(generalAlgorithms.isInZ_q_hat(s_1), "s_1 must be in Z_q_hat");
        Preconditions.checkArgument(generalAlgorithms.isMember(s_2), "s_2 must be in G_q");
        Preconditions.checkArgument(generalAlgorithms.isInZ_q(s_3), "s_3 must be in Z_q");

        Preconditions.checkArgument(pk.getEncryptionGroup() == publicParameters.getEncryptionGroup());

        log.debug(String.format("checkBallotProof: a = %s", a));

        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        BigInteger p_hat = publicParameters.getIdentificationGroup().getP_hat();
        BigInteger q_hat = publicParameters.getIdentificationGroup().getQ_hat();
        BigInteger g_hat = publicParameters.getIdentificationGroup().getG_hat();

        BigInteger[] y = new BigInteger[]{x_hat, a, b};
        BigInteger[] t_array = new BigInteger[3];
        t.toArray(t_array);
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, t_array, q.min(q_hat));
        log.debug(String.format("checkBallotProof: c = %s", c));

        BigInteger t_prime_1 = modExp(x_hat, c.negate(), p_hat).multiply(modExp(g_hat, s_1, p_hat)).mod(p_hat);
        BigInteger t_prime_2 = modExp(a, c.negate(), p).multiply(s_2).multiply(modExp(pk.getPublicKey(), s_3, p)).mod(p);
        BigInteger t_prime_3 = modExp(b, c.negate(), p).multiply(modExp(g, s_3, p)).mod(p);

        return t_array[0].compareTo(t_prime_1) == 0 &&
                t_array[1].compareTo(t_prime_2) == 0 &&
                t_array[2].compareTo(t_prime_3) == 0;
    }

    /**
     * Algorithm 7.25: GenResponse
     *
     * @param i            the voter index
     * @param bold_a       the vector of the queries
     * @param pk           the encryption public key
     * @param bold_n       the vector of number of candidates per election
     * @param bold_K       the matrix of number of selections per voter per election
     * @param upper_bold_p the matrix of points per voter per candidate
     * @return the OT response, along with the randomness used
     * @throws IncompatibleParametersException if not enough primes exist in the encryption group for the number of candidates
     */
    public ObliviousTransferResponseAndRand genResponse(Integer i, List<BigInteger> bold_a, EncryptionPublicKey pk,
                                                        List<Integer> bold_n,
                                                        List<List<Integer>> bold_K,
                                                        List<List<Point>> upper_bold_p) {
        Preconditions.checkArgument(bold_a.stream().allMatch(generalAlgorithms::isMember),
                "All queries a_i must be in G_q");
        Preconditions.checkArgument(pk.getPublicKey().compareTo(BigInteger.ONE) != 0,
                "The encryption key may not be 1");
        Preconditions.checkArgument(generalAlgorithms.isMember(pk.getPublicKey()),
                "The public key must be a member of G_q");

        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        Preconditions.checkArgument(upper_bold_p.stream().flatMap(Collection::stream)
                        .allMatch(point -> BigInteger.ZERO.compareTo(point.x) <= 0 &&
                                point.x.compareTo(p_prime) < 0 &&
                                BigInteger.ZERO.compareTo(point.y) <= 0 &&
                                point.y.compareTo(p_prime) < 0),
                "All points' coordinates must be in Z_p_prime");
        Preconditions.checkArgument(bold_K.size() > 0);
        final int t = bold_K.get(0).size();
        Preconditions.checkArgument(bold_K.stream().allMatch(bold_k_i -> bold_k_i.size() == t));

        final int n = bold_n.stream().reduce((a, b) -> a + b).orElse(0);
        Preconditions.checkArgument(upper_bold_p.size() > 0);
        Preconditions.checkArgument(upper_bold_p.stream().allMatch(bold_p_i -> bold_p_i.size() == n));

        final int k_sum = bold_K.get(i).stream().reduce((a, b) -> a + b).orElse(0);
        Preconditions.checkArgument(bold_a.size() == k_sum);

        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        int upper_l_m = publicParameters.getUpper_l_m();

        List<BigInteger> bold_b = new ArrayList<>();
        byte[][] bold_c = new byte[n][];
        List<BigInteger> bold_d = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        List<BigInteger> bold_p;
        try {
            bold_p = generalAlgorithms.getPrimes(n);
        } catch (NotEnoughPrimesInGroupException e) {
            throw new IncompatibleParametersException(e);
        }

        int u = 0; // index 0 based, as opposed to the specification 1 based
        int v = 0; // same comment

        for (int j = 0; j < t; j++) {
            BigInteger r_j = randomGenerator.randomInZq(q);

            Integer k_ij = bold_K.get(i).get(j);
            for (int l = 0; l < k_ij; l++) {
                bold_b.add(modExp(bold_a.get(u++), r_j, p));
            }

            Integer n_j = bold_n.get(j);
            for (int l = 0; l < n_j; l++) {
                Point point_iv = upper_bold_p.get(i).get(v);
                @SuppressWarnings("SuspiciousNameCombination")
                byte[] M_v = ByteArrayUtils.concatenate(
                        conversion.toByteArray(point_iv.x, upper_l_m / 2),
                        conversion.toByteArray(point_iv.y, upper_l_m / 2)
                );
                log.debug(String.format("Encoding point %s as %s", point_iv, Arrays.toString(M_v)));
                BigInteger k = modExp(bold_p.get(v), r_j, p);
                byte[] bold_upper_k = new byte[0];
                int upperbound = (int) Math.ceil((double) upper_l_m / (publicParameters.getSecurityParameters().getL() / 8.0));
                for (int z = 1; z <= upperbound; z++) {
                    bold_upper_k = ByteArrayUtils.concatenate(bold_upper_k, hash.recHash_L(k, BigInteger.valueOf(z)));
                }
                bold_upper_k = ByteArrayUtils.truncate(bold_upper_k, upper_l_m);
                bold_c[v] = ByteArrayUtils.xor(M_v, bold_upper_k);
                log.debug(String.format("bold_c[%d] = %s", v, Arrays.toString(bold_c[v])));
                v++;
            }

            bold_d.add(modExp(pk.getPublicKey(), r_j, p));
            bold_r.add(r_j);
        }

        ObliviousTransferResponse beta = new ObliviousTransferResponse(bold_b, bold_c, bold_d);
        return new ObliviousTransferResponseAndRand(beta, bold_r);
    }

}
