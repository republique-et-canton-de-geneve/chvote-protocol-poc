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

import ch.ge.ve.protopoc.service.model.Confirmation;
import ch.ge.ve.protopoc.service.model.FinalizationCodePart;
import ch.ge.ve.protopoc.service.model.NonInteractiveZKP;
import ch.ge.ve.protopoc.service.model.PublicParameters;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import ch.ge.ve.protopoc.service.support.ByteArrayUtils;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;
import static java.math.BigInteger.ZERO;
import static java.util.Collections.singletonList;

/**
 * Algorithms for the vote confirmation phase, on the voting client's side
 */
public class VoteConfirmationClientAlgorithms {
    private final PublicParameters publicParameters;
    private final RandomGenerator randomGenerator;
    private final GeneralAlgorithms generalAlgorithms;
    private final Hash hash;
    private final Conversion conversion = new Conversion();

    public VoteConfirmationClientAlgorithms(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms, RandomGenerator randomGenerator, Hash hash) {
        this.publicParameters = publicParameters;
        this.randomGenerator = randomGenerator;
        this.generalAlgorithms = generalAlgorithms;
        this.hash = hash;
    }

    /**
     * Algorithm 7.30: GenConfirmation
     *
     * @param upper_y            the confirmation code
     * @param upper_bold_p_prime the <tt>k</tt> per <tt>s</tt> point matrix, where k = sum(bold_k) and s is the number of authorities
     * @param bold_k             the number of selections per election
     * @return the public confirmation y_hat and the proof of knowledge of the secret confirmation y
     */
    public Confirmation genConfirmation(String upper_y, List<List<Point>> upper_bold_p_prime, List<Integer> bold_k) {
        Preconditions.checkNotNull(upper_y);
        Preconditions.checkNotNull(upper_bold_p_prime);

        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        Preconditions.checkArgument(upper_bold_p_prime.stream().flatMap(Collection::stream)
                        .allMatch(point -> BigInteger.ZERO.compareTo(point.x) <= 0 &&
                                point.x.compareTo(p_prime) < 0 &&
                                BigInteger.ZERO.compareTo(point.y) <= 0 &&
                                point.y.compareTo(p_prime) < 0),
                "All points' coordinates must be in Z_p_prime");
        Preconditions.checkArgument(upper_bold_p_prime.size() == publicParameters.getS());
        Preconditions.checkNotNull(bold_k);
        Preconditions.checkArgument(bold_k.stream().allMatch(k_j -> k_j >= 0),
                "All k_j's must be greater than or equal to 0");

        BigInteger p_hat = publicParameters.getIdentificationGroup().getP_hat();
        BigInteger q_hat = publicParameters.getIdentificationGroup().getQ_hat();
        BigInteger g_hat = publicParameters.getIdentificationGroup().getG_hat();

        List<BigInteger> h_js = IntStream.range(0, publicParameters.getS()).parallel()
                .mapToObj(upper_bold_p_prime::get)
                .map(bold_p_prime_j -> getValues(bold_p_prime_j, bold_k))
                .map(bold_y_j -> conversion.toInteger(hash.recHash_L(bold_y_j.toArray())).mod(q_hat))
                .collect(Collectors.toList());

        BigInteger y = conversion.toInteger(upper_y, publicParameters.getUpper_a_y())
                .add(h_js.stream().reduce(BigInteger::add).orElse(ZERO))
                .mod(q_hat);
        BigInteger y_hat = modExp(g_hat, y, p_hat);
        NonInteractiveZKP pi = genConfirmationProof(y, y_hat);

        return new Confirmation(y_hat, pi);
    }

    /**
     * Algorithm 7.31: GetValues
     *
     * @param bold_p the combined list of points for the <tt>t</tt> polynomials (1 per election)
     * @param bold_k the list of the number of selections allowed by election
     * @return the list of values for <tt>A_j(0)</tt>
     */
    public List<BigInteger> getValues(List<Point> bold_p, List<Integer> bold_k) {
        Preconditions.checkNotNull(bold_p);
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        Preconditions.checkArgument(bold_p.stream()
                        .allMatch(point -> BigInteger.ZERO.compareTo(point.x) <= 0 &&
                                point.x.compareTo(p_prime) < 0 &&
                                BigInteger.ZERO.compareTo(point.y) <= 0 &&
                                point.y.compareTo(p_prime) < 0),
                "All points' coordinates must be in Z_p_prime");
        Preconditions.checkNotNull(bold_k);
        Preconditions.checkArgument(bold_k.stream().allMatch(k_j -> k_j >= 0),
                "All k_j's must be greater than or equal to 0");

        List<BigInteger> bold_y = new ArrayList<>();
        int i = 0;
        for (Integer k_j : bold_k) {
            BigInteger y_i = getValue(bold_p.subList(i, i + k_j));
            bold_y.add(y_i);
            i += k_j;
        }

        return bold_y;
    }

    /**
     * Algorithm 7.32: GetValue
     *
     * @param bold_p a list of <tt>k</tt> points defining the polynomial <tt>A(X)</tt> of degree <tt>k - 1</tt>
     * @return the interpolated value <tt>y = A(0)</tt>
     */
    public BigInteger getValue(List<Point> bold_p) {
        Preconditions.checkNotNull(bold_p);
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        Preconditions.checkArgument(bold_p.stream()
                        .allMatch(point -> BigInteger.ZERO.compareTo(point.x) <= 0 &&
                                point.x.compareTo(p_prime) < 0 &&
                                BigInteger.ZERO.compareTo(point.y) <= 0 &&
                                point.y.compareTo(p_prime) < 0),
                "All points' coordinates must be in Z_p_prime");

        BigInteger y = ZERO;
        for (int i = 0; i < bold_p.size(); i++) {
            BigInteger n = BigInteger.ONE;
            BigInteger d = BigInteger.ONE;
            for (int j = 0; j < bold_p.size(); j++) {
                if (i != j) {
                    BigInteger x_i = bold_p.get(i).x;
                    BigInteger x_j = bold_p.get(j).x;

                    n = n.multiply(x_j).mod(p_prime);
                    d = d.multiply(x_j.subtract(x_i)).mod(p_prime);
                }
            }
            BigInteger y_i = bold_p.get(i).y;

            y = y.add(y_i.multiply(n.multiply(d.modInverse(p_prime)))).mod(p_prime);
        }

        return y;
    }

    /**
     * Algorithm 7.33: GenConfirmationProof
     *
     * @param y     the secret confirmation credential
     * @param y_hat the public confirmation credential
     * @return a proof of knowledge of the secret confirmation credential
     */
    public NonInteractiveZKP genConfirmationProof(BigInteger y, BigInteger y_hat) {
        BigInteger p_hat = publicParameters.getIdentificationGroup().getP_hat();
        BigInteger q_hat = publicParameters.getIdentificationGroup().getQ_hat();
        BigInteger g_hat = publicParameters.getIdentificationGroup().getG_hat();
        int tau = publicParameters.getSecurityParameters().getTau();

        //noinspection SuspiciousNameCombination
        Preconditions.checkArgument(generalAlgorithms.isInZ_q_hat(y), "y must be in Z_q_hat");
        //noinspection SuspiciousNameCombination
        Preconditions.checkArgument(generalAlgorithms.isMember_G_q_hat(y_hat),
                "y_hat must be in G_q_hat");

        BigInteger omega = randomGenerator.randomInZq(q_hat);

        BigInteger t = modExp(g_hat, omega, p_hat);
        BigInteger[] bold_v = new BigInteger[]{y_hat};
        BigInteger[] bold_t = new BigInteger[]{t};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(bold_v, bold_t, tau);

        BigInteger s = omega.add(c.multiply(y)).mod(q_hat);
        return new NonInteractiveZKP(singletonList(t), singletonList(s));
    }

    /**
     * Algorithm 7.38: GetFinalizationCode
     *
     * @param bold_delta the finalization code parts received from the authorities
     * @return the combined finalization code
     */
    public String getFinalizationCode(List<FinalizationCodePart> bold_delta) {
        Preconditions.checkArgument(bold_delta.size() == publicParameters.getS());
        return bold_delta.stream()
                .map(FinalizationCodePart::getF) // extract F_j
                .reduce(ByteArrayUtils::xor) // xor over j
                .map(b -> conversion.toString(b, publicParameters.getUpper_a_f()))
                .orElse("");
    }
}
