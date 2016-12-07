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
import java.util.List;

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
     * Algorithm 5.31: GenConfirmation
     *
     * @param Y      the confirmation code
     * @param bold_P the <tt>k</tt> per <tt>s</tt> point matrix, where k = sum(bold_k) and s is the number of authorities
     * @param bold_k the number of selections per election
     * @return the public confirmation y_circ and the proof of knowledge of the secret confirmation y
     */
    public Confirmation genConfirmation(String Y, List<List<Point>> bold_P, List<Integer> bold_k) {
        Preconditions.checkNotNull(Y);
        Preconditions.checkNotNull(bold_P);
        Preconditions.checkArgument(bold_P.size() == publicParameters.getS());
        Preconditions.checkNotNull(bold_k);

        BigInteger p_circ = publicParameters.getIdentificationGroup().getP_circ();
        BigInteger q_circ = publicParameters.getIdentificationGroup().getQ_circ();
        BigInteger g_circ = publicParameters.getIdentificationGroup().getG_circ();

        List<BigInteger> y_js = new ArrayList<>();
        for (int j = 0; j < publicParameters.getS(); j++) {
            List<Point> bold_p_j = bold_P.get(j);
            List<BigInteger> bold_y_j = getValues(bold_p_j, bold_k);
            BigInteger y_j = conversion.toInteger(hash.hash(bold_y_j.toArray())).mod(q_circ);
            y_js.add(y_j);
        }
        BigInteger y = conversion.toInteger(Y, publicParameters.getA_y()).add(
                y_js.stream().reduce(BigInteger::add).orElseThrow(
                        () -> new IllegalArgumentException("Can't happen if s > 0"))
        ).mod(q_circ);
        BigInteger y_circ = g_circ.modPow(y, p_circ);
        NonInteractiveZKP pi = genConfirmationNIZKP(y, y_circ);

        return new Confirmation(y_circ, pi);
    }

    /**
     * Algorithm 5.32: GetValues
     *
     * @param bold_p the combined list of points for the <tt>t</tt> polynomials (1 per election)
     * @param bold_k the list of the number of selections allowed by election
     * @return the list of values for <tt>A_j(0)</tt>
     */
    public List<BigInteger> getValues(List<Point> bold_p, List<Integer> bold_k) {
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
     * Algorithm 5.33: GetValue
     *
     * @param bold_p a list of <tt>k</tt> points defining the polynomial <tt>A(X)</tt> of degree <tt>k - 1</tt>
     * @return the interpolated value <tt>y = A(0)</tt>
     */
    public BigInteger getValue(List<Point> bold_p) {
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();

        BigInteger y = BigInteger.ZERO;
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
     * Algorithm 5.34: GenConfirmationNIZKP
     *
     * @param y      the secret confirmation credential
     * @param y_circ the public confirmation credential
     * @return a NIZKP of knowledge of the secret confirmation credential
     */
    public NonInteractiveZKP genConfirmationNIZKP(BigInteger y, BigInteger y_circ) {
        BigInteger p_circ = publicParameters.getIdentificationGroup().getP_circ();
        BigInteger q_circ = publicParameters.getIdentificationGroup().getQ_circ();
        BigInteger g_circ = publicParameters.getIdentificationGroup().getG_circ();

        BigInteger omega = randomGenerator.randomInZq(q_circ);

        BigInteger t = g_circ.modPow(omega, p_circ);
        BigInteger[] bold_v = new BigInteger[]{y_circ};
        BigInteger[] bold_t = new BigInteger[]{t};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(bold_v, bold_t, q_circ);

        BigInteger s = omega.add(c.multiply(y)).mod(q_circ);
        return new NonInteractiveZKP(singletonList(t), singletonList(s));
    }

    /**
     * Algorithm 5.35: GetFinalizationCode
     *
     * @param finalizationCodeParts the finalization code parts received from the authorities
     * @return the combined finalization code
     */
    public String getFinalizationCode(List<FinalizationCodePart> finalizationCodeParts) {
        Preconditions.checkArgument(finalizationCodeParts.size() == publicParameters.getS());
        return finalizationCodeParts.stream().map(FinalizationCodePart::getF).reduce(ByteArrayUtils::xor)
                .map(b -> conversion.toString(b, publicParameters.getA_f()))
                .orElseThrow(() -> new IllegalArgumentException("Can't happen if s > 0"));
    }
}
