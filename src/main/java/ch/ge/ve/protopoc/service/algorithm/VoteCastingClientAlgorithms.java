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

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersRuntimeException;
import ch.ge.ve.protopoc.service.exception.InvalidObliviousTransferResponseException;
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
import java.util.stream.Collectors;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Algorithms related to the vote casting phase
 */
public class VoteCastingClientAlgorithms {
    private static final Logger log = LoggerFactory.getLogger(VoteCastingClientAlgorithms.class);
    private final PublicParameters publicParameters;
    private final Hash hash;
    private final RandomGenerator randomGenerator;
    private final GeneralAlgorithms generalAlgorithms;
    private final Conversion conversion = new Conversion();

    public VoteCastingClientAlgorithms(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms, RandomGenerator randomGenerator, Hash hash) {
        this.publicParameters = publicParameters;
        this.hash = hash;
        this.randomGenerator = randomGenerator;
        this.generalAlgorithms = generalAlgorithms;
    }

    /**
     * Algorithm 7.18: GenBallot
     *
     * @param upper_x the voting code
     * @param bold_s  voters selection (indices)
     * @param pk      the public encryption key
     * @return the combined ballot, OT query and random elements used
     * @throws IncompatibleParametersRuntimeException when there is an issue with the public parameters
     */
    public BallotQueryAndRand genBallot(String upper_x, List<Integer> bold_s, EncryptionPublicKey pk) {
        Preconditions.checkArgument(bold_s.size() > 0,
                "There needs to be at least one selection");
        Preconditions.checkArgument(bold_s.stream().sorted().collect(Collectors.toList()).equals(bold_s),
                "The list of selections needs to be ordered");
        Preconditions.checkArgument(bold_s.stream().allMatch(i -> i >= 1),
                "Selections must be strictly positive");
        Preconditions.checkArgument(bold_s.stream().distinct().count() == bold_s.size(),
                "All selections must be distinct");
        Preconditions.checkArgument(generalAlgorithms.isMember(pk.getPublicKey()),
                "The key must be a member of G_q");
        Preconditions.checkArgument(BigInteger.ONE.compareTo(pk.getPublicKey()) != 0,
                "The key must not be 1");

        BigInteger p_hat = publicParameters.getIdentificationGroup().getP_hat();
        BigInteger g_hat = publicParameters.getIdentificationGroup().getG_hat();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();

        BigInteger x = conversion.toInteger(upper_x, publicParameters.getUpper_a_x());
        BigInteger x_hat = modExp(g_hat, x, p_hat);

        List<BigInteger> bold_q = computeBoldQ(bold_s);
        BigInteger m = computeM(bold_q, p);
        ObliviousTransferQuery query = genQuery(bold_q, pk);
        BigInteger a = computeA(query, p);
        BigInteger r = computeR(query, q);
        BigInteger b = modExp(g, r, p);
        NonInteractiveZKP pi = genBallotProof(x, m, r, x_hat, a, b, pk);
        BallotAndQuery alpha = new BallotAndQuery(x_hat, query.getBold_a(), b, pi);

        return new BallotQueryAndRand(alpha, query.getBold_r());
    }

    private List<BigInteger> computeBoldQ(List<Integer> bold_s) {
        List<BigInteger> bold_q;
        try {
            bold_q = getSelectedPrimes(bold_s);
        } catch (NotEnoughPrimesInGroupException e) {
            throw new IncompatibleParametersRuntimeException("Encryption Group too small for selection");
        }
        return bold_q;
    }

    private BigInteger computeM(List<BigInteger> bold_q, BigInteger p) {
        BigInteger m = bold_q.stream().reduce(BigInteger::multiply)
                .orElse(ONE);
        if (m.compareTo(p) >= 0) {
            throw new IncompatibleParametersRuntimeException("(k,n) is incompatible with p");
        }
        return m;
    }

    private BigInteger computeA(ObliviousTransferQuery query, BigInteger p) {
        return query.getBold_a().stream().reduce(BigInteger::multiply)
                .orElse(ONE)
                .mod(p);
    }

    private BigInteger computeR(ObliviousTransferQuery query, BigInteger q) {
        return query.getBold_r().stream().reduce(BigInteger::add)
                .orElse(ZERO)
                .mod(q);
    }


    /**
     * Algorithm 7.19: getSelectedPrimes
     *
     * @param bold_s the indices of the selected primes (in increasing order, 1-based)
     * @return the list of the primes selected
     */
    public List<BigInteger> getSelectedPrimes(List<Integer> bold_s) throws NotEnoughPrimesInGroupException {
        Preconditions.checkArgument(bold_s.size() > 0,
                "There needs to be at least one selection");
        Preconditions.checkArgument(bold_s.stream().allMatch(i -> i >= 1),
                "Selections must be strictly positive");
        Preconditions.checkArgument(
                bold_s.stream().sorted().collect(Collectors.toList()).equals(bold_s),
                "The elements must be sorted");
        Preconditions.checkArgument(bold_s.stream().distinct().count() == bold_s.size(),
                "All selections must be distinct");
        Integer s_k = bold_s.get(bold_s.size() - 1);
        List<BigInteger> primes = generalAlgorithms.getPrimes(s_k);

        return bold_s.stream()
                .map(s_i -> s_i - 1) // s_i is 1-based
                .map(primes::get)
                .collect(Collectors.toList());
    }

    /**
     * Algorithm 7.20: GenQuery
     *
     * @param bold_q the selected primes
     * @param pk     the public encryption key
     * @return the generated oblivious transfer query
     */
    public ObliviousTransferQuery genQuery(List<BigInteger> bold_q, EncryptionPublicKey pk) {
        Preconditions.checkArgument(generalAlgorithms.isMember(pk.getPublicKey()),
                "The key must be a member of G_q");
        Preconditions.checkArgument(BigInteger.ONE.compareTo(pk.getPublicKey()) != 0,
                "The key must not be 1");
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger p = publicParameters.getEncryptionGroup().getP();

        List<BigInteger> bold_a = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        for (BigInteger q_i : bold_q) {
            BigInteger r_i = randomGenerator.randomInZq(q);
            BigInteger a_i = q_i.multiply(modExp(pk.getPublicKey(), r_i, p)).mod(p);
            bold_a.add(a_i);
            bold_r.add(r_i);
        }

        return new ObliviousTransferQuery(bold_a, bold_r);
    }

    /**
     * Algorithm 7.21: GenBallotProof
     *
     * @param x     first half of voting credentials
     * @param m     encoded selections, m \isin G_q
     * @param r     randomization
     * @param x_hat second half of voting credentials
     * @param a     first half of ElGamal encryption
     * @param b     second half of ElGamal encryption
     * @param pk    encryption key
     * @return a non interactive proof of knowledge for the ballot
     */
    public NonInteractiveZKP genBallotProof(
            BigInteger x,
            BigInteger m,
            BigInteger r,
            BigInteger x_hat,
            BigInteger a,
            BigInteger b,
            EncryptionPublicKey pk) {
        Preconditions.checkArgument(generalAlgorithms.isInZ_q_hat(x),
                "The private credential must be in Z_q_hat");
        Preconditions.checkArgument(generalAlgorithms.isMember_G_q_hat(x_hat),
                "x_hat must be in G_q_hat");
        Preconditions.checkArgument(generalAlgorithms.isMember(m), "m must be in G_q");
        Preconditions.checkArgument(generalAlgorithms.isInZ_q(r), "r must be in Z_q");
        Preconditions.checkArgument(generalAlgorithms.isMember(a), "a must be in G_q");
        Preconditions.checkArgument(generalAlgorithms.isMember(b), "b must be in G_q");
        Preconditions.checkArgument(generalAlgorithms.isMember(pk.getPublicKey()),
                "The key must be a member of G_q");
        IdentificationGroup identificationGroup = publicParameters.getIdentificationGroup();
        BigInteger p_hat = identificationGroup.getP_hat();
        BigInteger q_hat = identificationGroup.getQ_hat();
        BigInteger g_hat = identificationGroup.getG_hat();

        EncryptionGroup encryptionGroup = publicParameters.getEncryptionGroup();
        BigInteger p = encryptionGroup.getP();
        BigInteger q = encryptionGroup.getQ();
        BigInteger g = encryptionGroup.getG();

        log.debug(String.format("genBallotProof: a = %s", a));

        BigInteger omega_1 = randomGenerator.randomInZq(q_hat);
        BigInteger omega_2 = randomGenerator.randomInGq(encryptionGroup);
        BigInteger omega_3 = randomGenerator.randomInZq(q);

        BigInteger t_1 = modExp(g_hat, omega_1, p_hat);
        BigInteger t_2 = omega_2.multiply(modExp(pk.getPublicKey(), omega_3, p)).mod(p);
        BigInteger t_3 = modExp(g, omega_3, p);

        BigInteger[] y = new BigInteger[]{x_hat, a, b};
        BigInteger[] t = new BigInteger[]{t_1, t_2, t_3};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, t, q.min(q_hat));
        log.debug(String.format("genBallotProof: c = %s", c));

        BigInteger s_1 = omega_1.add(c.multiply(x)).mod(q_hat);
        BigInteger s_2 = omega_2.multiply(modExp(m, c, p)).mod(p);
        BigInteger s_3 = omega_3.add(c.multiply(r)).mod(q);
        List<BigInteger> s = Arrays.asList(s_1, s_2, s_3);

        return new NonInteractiveZKP(Arrays.asList(t), s);
    }

    /**
     * Algorithm 7.26: GetPointMatrix
     *
     * @param bold_beta the vector of the oblivious transfer replies (from the different authorities)
     * @param bold_k    the vector of allowed number of selections per election
     * @param bold_s    the vector of selected primes
     * @param bold_r    the vector of randomizations used for the OT query
     * @return the point matrix corresponding to the replies of the s authorities for the k selections
     * @throws InvalidObliviousTransferResponseException when one of the points would be outside the defined space
     */
    public List<List<Point>> getPointMatrix(
            List<ObliviousTransferResponse> bold_beta,
            List<Integer> bold_k,
            List<Integer> bold_s,
            List<BigInteger> bold_r) throws InvalidObliviousTransferResponseException {
        Preconditions.checkArgument(bold_beta.stream().flatMap(beta -> beta.getB().stream())
                        .allMatch(generalAlgorithms::isMember),
                "All the b_j's in bold_beta must be in G_q");
        Preconditions.checkArgument(bold_beta.stream().flatMap(beta -> beta.getD().stream())
                        .allMatch(generalAlgorithms::isMember),
                "All the d_j's in bold_beta must be in G_q");
        Preconditions.checkArgument(bold_s.size() > 0,
                "There needs to be at least one selection");
        Preconditions.checkArgument(bold_s.stream().allMatch(i -> i >= 1),
                "Selections must be strictly positive");
        Preconditions.checkArgument(
                bold_s.stream().sorted().collect(Collectors.toList()).equals(bold_s),
                "The elements must be sorted");
        Preconditions.checkArgument(bold_s.stream().distinct().count() == bold_s.size(),
                "All selections must be distinct");
        final BigInteger q = publicParameters.getEncryptionGroup().getQ();
        Preconditions.checkArgument(bold_r.stream().allMatch(generalAlgorithms::isInZ_q),
                "All r_i must be in Z_q");
        List<List<Point>> bold_P = new ArrayList<>();

        for (ObliviousTransferResponse beta_j : bold_beta) {
            bold_P.add(getPoints(beta_j, bold_k, bold_s, bold_r));
        }

        return bold_P;
    }

    /**
     * Algorithm 7.27: GetPoints
     *
     * @param beta   the OT response (from one authority)
     * @param bold_k the vector of allowed number of selections per election
     * @param bold_s the vector of selected primes
     * @param bold_r the vector of randomizations used for the OT query
     * @return the points corresponding to the authority's reply for the k selections
     * @throws InvalidObliviousTransferResponseException when one of the points would be outside the defined space
     */
    public List<Point> getPoints(
            ObliviousTransferResponse beta,
            List<Integer> bold_k,
            List<Integer> bold_s,
            List<BigInteger> bold_r) throws InvalidObliviousTransferResponseException {
        Preconditions.checkArgument(beta.getB().stream().allMatch(generalAlgorithms::isMember),
                "All the b_j's in bold_beta must be in G_q");
        Preconditions.checkArgument(beta.getD().stream().allMatch(generalAlgorithms::isMember),
                "All the d_j's in bold_beta must be in G_q");
        Preconditions.checkArgument(bold_s.size() > 0,
                "There needs to be at least one selection");
        Preconditions.checkArgument(bold_s.stream().allMatch(i -> i >= 1),
                "Selections must be strictly positive");
        Preconditions.checkArgument(
                bold_s.stream().sorted().collect(Collectors.toList()).equals(bold_s),
                "The elements must be sorted");
        Preconditions.checkArgument(bold_s.stream().distinct().count() == bold_s.size(),
                "All selections must be distinct");
        List<Point> bold_p = new ArrayList<>();
        List<BigInteger> b = beta.getB();
        byte[][] c = beta.getC();
        List<BigInteger> d = beta.getD();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        int upper_l_m = publicParameters.getUpper_l_m();

        int i = 0; // 0 based indices in java, as opposed to the 1-based specification
        for (int j = 0; j < bold_k.size(); j++) {
            for (int l = 0; l < bold_k.get(j); l++) {
                log.debug("c[" + (bold_s.get(i) - 1) + "] = " + Arrays.toString(c[bold_s.get(i) - 1]));
                BigInteger k = b.get(i).multiply(modExp(d.get(j), bold_r.get(i).negate(), p)).mod(p);
                byte[] bold_upper_k = computeBoldUpperK(upper_l_m, k);
                byte[] M_i = ByteArrayUtils.xor(
                        // selections are 1-based
                        c[bold_s.get(i) - 1],
                        bold_upper_k);
                BigInteger x_i = conversion.toInteger(ByteArrayUtils.extract(M_i, 0, upper_l_m / 2));
                BigInteger y_i = conversion.toInteger(ByteArrayUtils.extract(M_i, upper_l_m / 2, M_i.length));
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Decoding %s as  point : %d <%s, %s>", Arrays.toString(M_i), i, x_i, y_i));
                }

                if (x_i.compareTo(p_prime) >= 0 || y_i.compareTo(p_prime) >= 0) {
                    throw new InvalidObliviousTransferResponseException("x_i >= p' or y_i >= p'");
                }
                bold_p.add(new Point(x_i, y_i));
                i++;
            }
        }

        return bold_p;
    }

    private byte[] computeBoldUpperK(int upper_l_m, BigInteger k) {
        byte[] bold_upper_k = new byte[0];
        int l_m = (int) Math.ceil((double) upper_l_m / publicParameters.getSecurityParameters().getUpper_l());
        for (int z = 1; z <= l_m; z++) {
            bold_upper_k = ByteArrayUtils.concatenate(bold_upper_k, hash.recHash_L(k, BigInteger.valueOf(z)));
        }
        bold_upper_k = ByteArrayUtils.truncate(bold_upper_k, upper_l_m);
        return bold_upper_k;
    }

    /**
     * Algorithm 7.28: GetReturnCodes
     *
     * @param bold_s the list of selections
     * @param bold_P the point matrix containing the responses for each of the authorities
     * @return the verification codes corresponding to the point matrix
     */
    public List<String> getReturnCodes(List<Integer> bold_s, List<List<Point>> bold_P) {
        int length = bold_P.get(0).size();
        Preconditions.checkArgument(bold_P.stream().allMatch(l -> l.size() == length));
        List<Character> A_r = publicParameters.getUpper_a_r();

        List<String> bold_rc_s = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            byte[] rc_i = new byte[publicParameters.getUpper_l_r()];
            for (int j = 0; j < publicParameters.getS(); j++) {
                rc_i = ByteArrayUtils.xor(rc_i, ByteArrayUtils.truncate(
                        hash.recHash_L(bold_P.get(j).get(i)),
                        publicParameters.getUpper_l_r()));
            }
            byte[] upper_r = ByteArrayUtils.markByteArray(rc_i, bold_s.get(i) - 1, publicParameters.getN_max());
            bold_rc_s.add(conversion.toString(upper_r, A_r));
        }
        return bold_rc_s;
    }
}
