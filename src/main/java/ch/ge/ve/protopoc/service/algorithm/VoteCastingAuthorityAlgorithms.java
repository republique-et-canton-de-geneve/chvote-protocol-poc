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
import java.util.List;

import static java.math.BigInteger.ONE;

/**
 * Algorithms related to the vote casting phase, performed by the authorities
 */
public class VoteCastingAuthorityAlgorithms {
    private static final Logger log = LoggerFactory.getLogger(VoteConfirmationAuthorityAlgorithms.class);
    private final PublicParameters publicParameters;
    private final GeneralAlgorithms generalAlgorithms;
    private final RandomGenerator randomGenerator;
    private final Hash hash;
    private final Conversion conversion = new Conversion();

    public VoteCastingAuthorityAlgorithms(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms, RandomGenerator randomGenerator, Hash hash) {
        this.publicParameters = publicParameters;
        this.generalAlgorithms = generalAlgorithms;
        this.randomGenerator = randomGenerator;
        this.hash = hash;
    }

    /**
     * Algorithm 5.25: CheckBallot
     *
     * @param i           the voter index
     * @param alpha       the submitted ballot, including the oblivious transfer query
     * @param pk          the encryption public key
     * @param bold_x_circ the vector of public voter credentials
     * @param B           the current ballot list
     * @return
     */
    public boolean checkBallot(Integer i, BallotAndQuery alpha, EncryptionPublicKey pk,
                               List<BigInteger> bold_x_circ, List<BallotEntry> B) {
        Preconditions.checkNotNull(i);
        Preconditions.checkNotNull(alpha);
        Preconditions.checkNotNull(alpha.getBold_a());
        Preconditions.checkArgument(alpha.getBold_a().size() > 0);
        Preconditions.checkNotNull(pk);
        Preconditions.checkNotNull(bold_x_circ);
        Preconditions.checkElementIndex(i, bold_x_circ.size());
        Preconditions.checkNotNull(B);

        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger x_circ_i = bold_x_circ.get(i);
        if (!hasBallot(i, B) && alpha.getX_circ().compareTo(x_circ_i) == 0) {
            BigInteger a = alpha.getBold_a().stream().reduce(BigInteger::multiply)
                    .orElse(ONE)
                    .mod(p);
            return checkBallotProof(alpha.getPi(), alpha.getX_circ(), a, alpha.getB(), pk);
        }
        return false;
    }

    /**
     * Algorithm 5.26: HasBallot
     *
     * @param i the voter index
     * @param B the current ballot list
     * @return true if any ballot in the list matches the given voter index, false otherwise
     */
    public boolean hasBallot(Integer i, List<BallotEntry> B) {
        Preconditions.checkNotNull(i);
        Preconditions.checkNotNull(B);

        return B.stream().anyMatch(b_j -> b_j.getI().equals(i));
    }

    /**
     * Algorithm 5.27: CheckBallotProof
     *
     * @param pi     the proof
     * @param x_circ public voting credential
     * @param a      first part of ElGamal encryption
     * @param b      second part of ElGamal encryption
     * @param pk     the encryption public key
     * @return true if the proof is valid, false otherwise
     */
    public boolean checkBallotProof(NonInteractiveZKP pi, BigInteger x_circ, BigInteger a, BigInteger b,
                                    EncryptionPublicKey pk) {
        Preconditions.checkNotNull(pi);
        Preconditions.checkNotNull(pi.getT());
        Preconditions.checkNotNull(pi.getS());
        Preconditions.checkNotNull(x_circ);
        Preconditions.checkNotNull(a);
        Preconditions.checkNotNull(b);
        Preconditions.checkNotNull(pk);
        Preconditions.checkNotNull(pk.getPublicKey());
        Preconditions.checkArgument(pk.getEncryptionGroup() == publicParameters.getEncryptionGroup());

        log.debug(String.format("checkBallotProof: a = %s", a));

        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        BigInteger p_circ = publicParameters.getIdentificationGroup().getP_circ();
        BigInteger q_circ = publicParameters.getIdentificationGroup().getQ_circ();
        BigInteger g_circ = publicParameters.getIdentificationGroup().getG_circ();

        BigInteger[] y = new BigInteger[]{x_circ, a, b};
        BigInteger[] t = new BigInteger[3];
        pi.getT().toArray(t);
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, t, q.min(q_circ));
        log.debug(String.format("checkBallotProof: c = %s", c));

        BigInteger s_1 = pi.getS().get(0);
        BigInteger s_2 = pi.getS().get(1);
        BigInteger s_3 = pi.getS().get(2);

        BigInteger t_prime_1 = x_circ.modPow(c.negate(), p_circ).multiply(g_circ.modPow(s_1, p_circ)).mod(p_circ);
        BigInteger t_prime_2 = a.modPow(c.negate(), p).multiply(s_2).multiply(pk.getPublicKey().modPow(s_3, p)).mod(p);
        BigInteger t_prime_3 = b.modPow(c.negate(), p).multiply(g.modPow(s_3, p)).mod(p);

        return t[0].compareTo(t_prime_1) == 0 &&
                t[1].compareTo(t_prime_2) == 0 &&
                t[2].compareTo(t_prime_3) == 0;
    }

    /**
     * Algorithm 5.28: GenResponse
     *
     * @param i      the voter index
     * @param bold_a the vector of the queries
     * @param pk     the encryption public key
     * @param bold_n the vector of number of candidates per election
     * @param bold_K the matrix of number of selections per voter per election
     * @param bold_P the matrix of points per voter per candidate
     * @return the OT response, along with the randomness used
     * @throws IncompatibleParametersException if not enough primes exist in the encryption group for the number of candidates
     */
    public ObliviousTransferResponseAndRand genResponse(Integer i, List<BigInteger> bold_a, EncryptionPublicKey pk,
                                                        List<Integer> bold_n,
                                                        List<List<Integer>> bold_K,
                                                        List<List<Point>> bold_P)
            throws IncompatibleParametersException {
        Preconditions.checkArgument(bold_K.size() > 0);
        final int t = bold_K.get(0).size();
        Preconditions.checkArgument(bold_K.stream().allMatch(bold_k_i -> bold_k_i.size() == t));

        final int n = bold_n.stream().reduce((a, b) -> a + b).orElse(0);
        Preconditions.checkArgument(bold_P.size() > 0);
        Preconditions.checkArgument(bold_P.stream().allMatch(bold_p_i -> bold_p_i.size() == n));

        final int k = bold_K.get(i).stream().reduce((a, b) -> a + b).orElse(0);
        Preconditions.checkArgument(bold_a.size() == k);

        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        int upper_l_m = publicParameters.getL_m() / 8;

        List<BigInteger> bold_b = new ArrayList<>();
        byte[][] bold_c = new byte[n][];
        List<BigInteger> bold_d = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        List<BigInteger> bold_u;
        try {
            bold_u = generalAlgorithms.getPrimes(n);
        } catch (NotEnoughPrimesInGroupException e) {
            throw new IncompatibleParametersException(e);
        }

        // u = k_offset + l \in [0, k_ij)
        int k_offset = 0; // index 0 based, as opposed to the specification 1 based
        // v = n_offset + l \in [0, n_j)
        int n_offset = 0; // same comment

        for (int j = 0; j < t; j++) {
            BigInteger r_j = randomGenerator.randomInZq(q);

            Integer k_ij = bold_K.get(i).get(j);
            for (int l = 0; l < k_ij; l++) {
                bold_b.add(bold_a.get(k_offset + l).modPow(r_j, p));
            }
            k_offset += k_ij;

            Integer n_j = bold_n.get(j);
            for (int l = 0; l < n_j; l++) {
                int v = n_offset + l;
                Point point_iv = bold_P.get(i).get(v);
                byte[] M_v = ByteArrayUtils.concatenate(
                        conversion.toByteArray(point_iv.x, upper_l_m / 2),
                        conversion.toByteArray(point_iv.y, upper_l_m / 2)
                );
                log.debug(String.format("Encoding point %s as %s", point_iv, Arrays.toString(M_v)));
                BigInteger valueToHash = bold_u.get(v).modPow(r_j, p);
                log.debug(String.format("Hashing the following value: %s", valueToHash));
                bold_c[v] = ByteArrayUtils.xor(M_v, Arrays.copyOf(hash.hash(valueToHash), upper_l_m));
                log.debug(String.format("bold_c[%d] = %s", v, Arrays.toString(bold_c[v])));
            }
            n_offset += n_j;

            bold_d.add(pk.getPublicKey().modPow(r_j, p));
            bold_r.add(r_j);
        }

        ObliviousTransferResponse beta = new ObliviousTransferResponse(bold_b, bold_c, bold_d);
        return new ObliviousTransferResponseAndRand(beta, bold_r);
    }

}
