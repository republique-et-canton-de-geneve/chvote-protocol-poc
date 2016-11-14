package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.InvalidObliviousTransferResponse;
import ch.ge.ve.protopoc.service.model.ObliviousTransferResponse;
import ch.ge.ve.protopoc.service.model.PublicParameters;
import ch.ge.ve.protopoc.service.support.ByteArrayUtils;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Missing javadoc!
 */
public class VoteCasting {
    private final PublicParameters publicParameters;
    private final Hash hash;
    private final Conversion conversion = new Conversion();

    public VoteCasting(PublicParameters publicParameters, Hash hash) {
        this.publicParameters = publicParameters;
        this.hash = hash;
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
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public List<List<Polynomial.Point>> getPointMatrix(
            List<ObliviousTransferResponse> bold_beta,
            List<Integer> bold_k,
            List<Integer> bold_s,
            List<BigInteger> bold_r) throws InvalidObliviousTransferResponse, NoSuchAlgorithmException, NoSuchProviderException {
        List<List<Polynomial.Point>> bold_P = new ArrayList<>();

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
    public List<Polynomial.Point> getPoints(
            ObliviousTransferResponse beta,
            List<Integer> bold_k,
            List<Integer> bold_s,
            List<BigInteger> bold_r) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidObliviousTransferResponse {
        List<Polynomial.Point> bold_p = new ArrayList<>();
        List<BigInteger> b = beta.getB();
        byte[][] c = beta.getC();
        List<BigInteger> d = beta.getD();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger p_prime = publicParameters.getPrimeField().getP_prime();
        int L_m = publicParameters.getL_m() / 8;

        int i = 0; // 0 based indices in java, as opposed to the 1-based specification
        for (int j = 0; j < bold_k.size(); j++) {
            for (int l = 0; l < bold_k.get(j); l++) {
                byte[] M_i = ByteArrayUtils.xor(c[bold_s.get(i)], hash.hash(b.get(i).multiply(d.get(j).modPow(bold_r.get(j).negate(), p))));
                BigInteger x_i = conversion.toInteger(Arrays.copyOfRange(M_i, 0, L_m / 2 + 1));
                BigInteger y_i = conversion.toInteger(Arrays.copyOfRange(M_i, L_m / 2 + 1, M_i.length));
                if (x_i.compareTo(p_prime) >= 0 || y_i.compareTo(p_prime) >= 0) {
                    throw new InvalidObliviousTransferResponse("x_i >= p' or y_i >= p'");
                }
                bold_p.add(new Polynomial.Point(x_i, y_i));
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
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public byte[][] getReturnCodes(List<List<Polynomial.Point>> bold_P) throws NoSuchProviderException, NoSuchAlgorithmException {
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
