package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.support.BigIntegers;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.MoreObjects;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Algorithms relevant to the election preparation
 */
public class ElectionPreparation {
    private final Hash hash;
    private final RandomGenerator randomGenerator;
    private final BigInteger q_x;
    private final BigInteger q_y;
    private final IdentificationGroup identificationGroup;
    private final Conversion conversion = new Conversion();
    private final Polynomial polynomial;

    public ElectionPreparation(Hash hash, RandomGenerator randomGenerator, PublicParameters publicParameters) {
        this.hash = hash;
        this.randomGenerator = randomGenerator;
        q_x = BigIntegers.TWO.pow(publicParameters.getL_x()).divide(BigInteger.valueOf(publicParameters.getS()));
        q_y = BigIntegers.TWO.pow(publicParameters.getL_y()).divide(BigInteger.valueOf(publicParameters.getS()));
        identificationGroup = publicParameters.getIdentificationGroup();
        polynomial = new Polynomial(randomGenerator, publicParameters.getPrimeField());
    }

    /**
     * Algorithm 5.9: GenElectorateData
     *
     * @param electionSet contains all three of <b>n</b>, <b>k</b> and <b>E</b>
     * @return the generated electorate data, including private and public voter data
     */
    public ElectorateData genElectorateData(ElectionSet electionSet) throws NoSuchProviderException, NoSuchAlgorithmException {
        List<SecretVoterData> secretVoterDataList = new ArrayList<>();
        List<Polynomial.Point> publicVoterDataList = new ArrayList<>();
        List<List<Polynomial.Point>> randomPoints = new ArrayList<>();
        List<List<Integer>> allowedSelections = new ArrayList<>();

        // for i = 1, ..., N
        for (Voter voter : electionSet.getVoters()) {
            // for j = 1, ..., t
            List<Election> elections = electionSet.getElections();
            List<Integer> n = elections.stream()
                    .map(Election::getNumberOfCandidates)
                    .collect(Collectors.toList());
            List<Integer> k_i = elections.stream()
                    .map(e -> electionSet.isEligible(voter, e) ? e.getNumberOfSelections() : 0)
                    .collect(Collectors.toList());
            Polynomial.PointsAndZeroImages pointsAndZeroImages = polynomial.genPoints(n, k_i);
            SecretVoterData d_i = genSecretVoterData(pointsAndZeroImages.getPoints());
            secretVoterDataList.add(d_i);
            publicVoterDataList.add(getPublicVoterData(d_i.x, d_i.y, pointsAndZeroImages.getY0s()));
            randomPoints.add(pointsAndZeroImages.getPoints());
            allowedSelections.add(k_i);
        }

        return new ElectorateData(secretVoterDataList, publicVoterDataList, randomPoints, allowedSelections);
    }

    /**
     * Algorithm 5.10: GenSecretVoterData
     *
     * @param points a list of points, p_i \in Z^2_{p'}
     * @return the secret data for a single voter
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public SecretVoterData genSecretVoterData(List<Polynomial.Point> points) throws NoSuchProviderException, NoSuchAlgorithmException {
        BigInteger x = randomGenerator.randomBigInteger(q_x);
        BigInteger y = randomGenerator.randomBigInteger(q_y);
        byte[] F = hash.hash(points.toArray());
        byte[][] rc = new byte[points.size()][];

        for (int i = 0; i < points.size(); i++) {
            rc[i] = hash.hash(points.get(i));
        }

        return new SecretVoterData(x, y, F, rc);
    }

    /**
     * Algorithm 5.11: GetPublicVoterData
     *
     * @param x     secret voting credential x &isin; &integers;_q_circ
     * @param y     secret confirmation credential y &isin; &integers;_q_circ
     * @param yList (y_1, ..., y_t), y_i &isin; &integers;_p'
     * @return the public data for a single voter, sent to the bulletin board
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public Polynomial.Point getPublicVoterData(BigInteger x, BigInteger y, List<BigInteger> yList) throws NoSuchProviderException, NoSuchAlgorithmException {
        BigInteger local_y = y.add(conversion.toInteger(hash.hash(yList.toArray()))).mod(identificationGroup.getQ_circ());
        BigInteger x_circ = identificationGroup.getG_circ().modPow(x, identificationGroup.getP_circ());
        BigInteger y_circ = identificationGroup.getG_circ().modPow(local_y, identificationGroup.getP_circ());

        return new Polynomial.Point(x_circ, y_circ);
    }

    public static class SecretVoterData {
        private final BigInteger x;
        private final BigInteger y;
        private final byte[] F;
        private final byte[][] rc;

        public SecretVoterData(BigInteger x, BigInteger y, byte[] f, byte[][] rc) {
            this.x = x;
            this.y = y;
            F = f;
            this.rc = rc;
        }

        public BigInteger getX() {
            return x;
        }

        public BigInteger getY() {
            return y;
        }

        public byte[] getF() {
            return F;
        }

        public byte[][] getRc() {
            return rc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SecretVoterData that = (SecretVoterData) o;
            return Objects.equals(x, that.x) &&
                    Objects.equals(y, that.y) &&
                    Arrays.equals(F, that.F) &&
                    Arrays.deepEquals(rc, that.rc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, F, rc);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("x", x)
                    .add("y", y)
                    .add("F", F)
                    .add("rc", rc)
                    .toString();
        }
    }

    public static class ElectorateData {
        private final List<SecretVoterData> d;
        private final List<Polynomial.Point> d_circ;
        private final List<List<Polynomial.Point>> P;
        private final List<List<Integer>> K;

        public ElectorateData(List<SecretVoterData> secretVoterDataList, List<Polynomial.Point> publicVoterDataList, List<List<Polynomial.Point>> randomPoints, List<List<Integer>> allowedSelections) {
            this.d = secretVoterDataList;
            this.d_circ = publicVoterDataList;
            this.P = randomPoints;
            this.K = allowedSelections;
        }

        public List<SecretVoterData> getD() {
            return d;
        }

        public List<Polynomial.Point> getD_circ() {
            return d_circ;
        }

        public List<List<Polynomial.Point>> getP() {
            return P;
        }

        public List<List<Integer>> getK() {
            return K;
        }
    }
}
