package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.InvalidObliviousTransferResponse;
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import ch.ge.ve.protopoc.service.support.ByteArrayUtils;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Algorithms related to the vote casting phase
 */
public class VoteCastingClient {
    private final PublicParameters publicParameters;
    private final Hash hash;
    private final RandomGenerator randomGenerator;
    private final GeneralAlgorithms generalAlgorithms;
    private final Conversion conversion = new Conversion();

    public VoteCastingClient(PublicParameters publicParameters, Hash hash, RandomGenerator randomGenerator, GeneralAlgorithms generalAlgorithms) {
        this.publicParameters = publicParameters;
        this.hash = hash;
        this.randomGenerator = randomGenerator;
        this.generalAlgorithms = generalAlgorithms;
    }

    /**
     * Algorithm 5.19: GenBallot
     *
     * @param X      the voting code
     * @param bold_s voters selection (indices)
     * @param pk     the public encryption key
     * @return the combined ballot, OT query and random elements used
     * @throws NotEnoughPrimesInGroupException
     * @throws IncompatibleParametersException
     */
    public BallotQueryAndRand genBallot(byte[] X, List<Integer> bold_s, EncryptionPublicKey pk) throws IncompatibleParametersException {
        Preconditions.checkArgument(bold_s.size() > 0);
        BigInteger p_circ = publicParameters.getIdentificationGroup().getP_circ();
        BigInteger g_circ = publicParameters.getIdentificationGroup().getG_circ();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger g = publicParameters.getEncryptionGroup().getG();

        BigInteger x = conversion.toInteger(X);
        BigInteger x_circ = g_circ.modPow(x, p_circ);

        List<BigInteger> bold_u;
        try {
            bold_u = generalAlgorithms.getSelectedPrimes(bold_s);
        } catch (NotEnoughPrimesInGroupException e) {
            throw new IncompatibleParametersException("Encryption Group too small for selection");
        }
        BigInteger u = bold_u.stream().reduce(BigInteger::multiply)
                .orElseThrow(() -> new IllegalArgumentException("can't occur if bold_s is not empty"));
        if (u.compareTo(p) >= 0) {
            throw new IncompatibleParametersException("(k,n) is incompatible with p");
        }
        ObliviousTransferQuery query = genQuery(bold_u, pk);
        BigInteger a = query.getBold_a().stream().reduce(BigInteger::multiply)
                .orElseThrow(() -> new IllegalArgumentException("can't occur if bold_s is not empty"))
                .mod(p);
        BigInteger r = query.getBold_r().stream().reduce(BigInteger::add)
                .orElseThrow(() -> new IllegalArgumentException("can't occur if bold_s is not empty"))
                .mod(p);
        BigInteger b = g.modPow(r, p);
        NonInteractiveZKP pi = genBallotNIZKP(x, u, r, x_circ, a, b, pk);
        BallotAndQuery alpha = new BallotAndQuery(x_circ, query.getBold_a(), b, pi);

        return new BallotQueryAndRand(alpha, query.getBold_r());
    }

    /**
     * Algorithm 5.20: GenQuery
     *
     * @param bold_u the selected primes
     * @param pk     the public encryption key
     * @return the generated oblivious transfer query
     */
    public ObliviousTransferQuery genQuery(List<BigInteger> bold_u, EncryptionPublicKey pk) {
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger p = publicParameters.getEncryptionGroup().getP();

        List<BigInteger> bold_a = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        for (int i = 0; i < bold_u.size(); i++) {
            BigInteger r_i = randomGenerator.randomInZq(q);
            BigInteger a_i = bold_u.get(i).multiply(pk.getPublicKey().modPow(r_i, p)).mod(p);
            bold_a.add(a_i);
            bold_r.add(r_i);
        }

        return new ObliviousTransferQuery(bold_a, bold_r);
    }

    /**
     * Algorithm 5.21: GenBallotNIZKP
     *
     * @param x      first half of voting credentials
     * @param u      encoded selections, u \isin G_q
     * @param r      randomization
     * @param x_circ second half of voting credentials
     * @param a      first half of ElGamal encryption
     * @param b      second half of ElGamal encryption
     * @param pk     encryption key
     * @return a non interactive proof of knowledge for the ballot
     */
    public NonInteractiveZKP genBallotNIZKP(
            BigInteger x,
            BigInteger u,
            BigInteger r,
            BigInteger x_circ,
            BigInteger a, BigInteger b,
            EncryptionPublicKey pk) {
        IdentificationGroup identificationGroup = publicParameters.getIdentificationGroup();
        BigInteger p_circ = identificationGroup.getP_circ();
        BigInteger q_circ = identificationGroup.getQ_circ();
        BigInteger g_circ = identificationGroup.getG_circ();

        EncryptionGroup encryptionGroup = publicParameters.getEncryptionGroup();
        BigInteger p = encryptionGroup.getP();
        BigInteger q = encryptionGroup.getQ();
        BigInteger g = encryptionGroup.getG();

        BigInteger omega_1 = randomGenerator.randomInZq(q_circ);
        BigInteger omega_2 = randomGenerator.randomInGq(encryptionGroup);
        BigInteger omega_3 = randomGenerator.randomInZq(q);

        BigInteger t_1 = g_circ.modPow(omega_1, p_circ);
        BigInteger t_2 = omega_2.multiply(pk.getPublicKey().modPow(omega_3, p).mod(p));
        BigInteger t_3 = g.modPow(omega_3, p);

        BigInteger[] v = new BigInteger[]{x_circ, a, b};
        BigInteger[] t = new BigInteger[]{t_1, t_2, t_3};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(v, t, q.min(q_circ));

        BigInteger s_1 = omega_1.add(c.multiply(x)).mod(q_circ);
        BigInteger s_2 = omega_2.multiply(u.modPow(c, p)).mod(p);
        BigInteger s_3 = omega_3.add(c.multiply(r)).mod(q);
        List<BigInteger> s = Arrays.asList(s_1, s_2, s_3);

        return new NonInteractiveZKP(Arrays.asList(t), s);
    }

    /**
     * Algorithm 5.22: GetPointMatrix
     *
     * @param bold_beta the vector of the oblivious transfer replies (from the different authorities)
     * @param bold_k    the vector of allowed number of selections per election
     * @param bold_s    the vector of selected primes
     * @param bold_r    the vector of randomizations used for the OT query
     * @return the point matrix corresponding to the replies of the s authorities for the k selections
     * @throws InvalidObliviousTransferResponse
     */
    public List<List<Point>> getPointMatrix(
            List<ObliviousTransferResponse> bold_beta,
            List<Integer> bold_k,
            List<Integer> bold_s,
            List<BigInteger> bold_r) throws InvalidObliviousTransferResponse {
        List<List<Point>> bold_P = new ArrayList<>();

        for (ObliviousTransferResponse beta_j : bold_beta) {
            bold_P.add(getPoints(beta_j, bold_k, bold_s, bold_r));
        }

        return bold_P;
    }

    /**
     * Algorithm 5.23: GetPoints
     *
     * @param beta   the OT response (from one authority)
     * @param bold_k the vector of allowed number of selections per election
     * @param bold_s the vector of selected primes
     * @param bold_r the vector of randomizations used for the OT query
     * @return the points corresponding to the authority's reply for the k selections
     */
    public List<Point> getPoints(
            ObliviousTransferResponse beta,
            List<Integer> bold_k,
            List<Integer> bold_s,
            List<BigInteger> bold_r) throws InvalidObliviousTransferResponse {
        List<Point> bold_p = new ArrayList<>();
        List<BigInteger> b = beta.getB();
        byte[][] c = beta.getC();
        List<BigInteger> d = beta.getD();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        int L_m = publicParameters.getL_m() / 8;

        int i = 0; // 0 based indices in java, as opposed to the 1-based specification
        for (int j = 0; j < bold_k.size(); j++) {
            for (int l = 0; l < bold_k.get(j); l++) {
                byte[] M_i = ByteArrayUtils.xor(c[bold_s.get(i)], hash.hash(b.get(i).multiply(d.get(j).modPow(bold_r.get(j).negate(), p)).mod(p)));
                BigInteger x_i = conversion.toInteger(Arrays.copyOfRange(M_i, 0, L_m / 2 + 1));
                BigInteger y_i = conversion.toInteger(Arrays.copyOfRange(M_i, L_m / 2 + 1, M_i.length));
                if (x_i.compareTo(p_prime) >= 0 || y_i.compareTo(p_prime) >= 0) {
                    throw new InvalidObliviousTransferResponse("x_i >= p' or y_i >= p'");
                }
                bold_p.add(new Point(x_i, y_i));
                i++;
            }
        }

        return bold_p;
    }

    /**
     * Algorithm 5.24: GetReturnCodes
     *
     * @param bold_P the point matrix containing the responses for each of the authorities
     * @return the return codes corresponding to the point matrix
     */
    public byte[][] getReturnCodes(List<List<Point>> bold_P) {
        Preconditions.checkArgument(bold_P.size() == publicParameters.getS());
        int length = bold_P.get(0).size();
        Preconditions.checkArgument(bold_P.stream().allMatch(l -> l.size() == length));
        byte[][] rc = new byte[length][];
        for (int i = 0; i < length; i++) {
            byte[] rc_i = new byte[publicParameters.getL_r() / 8];
            for (int j = 0; j < publicParameters.getS(); j++) {
                rc_i = ByteArrayUtils.xor(rc_i, hash.hash(bold_P.get(j).get(i)));
            }
            rc[i] = rc_i;
        }
        return rc;
    }
}
