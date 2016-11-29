package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import ch.ge.ve.protopoc.service.model.polynomial.PointsAndZeroImages;
import ch.ge.ve.protopoc.service.support.BigIntegers;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Algorithms relevant to the election preparation
 */
public class ElectionPreparationAlgorithms {
    private final Hash hash;
    private final RandomGenerator randomGenerator;
    private final BigInteger q_x;
    private final BigInteger q_y;
    private final IdentificationGroup identificationGroup;
    private final Conversion conversion = new Conversion();
    private final PolynomialAlgorithms polynomialAlgorithms;
    private final int s;
    private final PublicParameters publicParameters;

    public ElectionPreparationAlgorithms(PublicParameters publicParameters, RandomGenerator randomGenerator, Hash hash) {
        this.hash = hash;
        this.randomGenerator = randomGenerator;
        this.publicParameters = publicParameters;
        q_x = BigIntegers.TWO.pow(publicParameters.getL_x()).divide(BigInteger.valueOf(publicParameters.getS()));
        q_y = BigIntegers.TWO.pow(publicParameters.getL_y()).divide(BigInteger.valueOf(publicParameters.getS()));
        identificationGroup = publicParameters.getIdentificationGroup();
        polynomialAlgorithms = new PolynomialAlgorithms(randomGenerator, publicParameters.getPrimeField());
        s = publicParameters.getS();
    }

    /**
     * Algorithm 5.9: GenElectorateData
     *
     * @param electionSet contains all three of <b>n</b>, <b>k</b> and <b>E</b>
     * @return the generated electorate data, including private and public voter data
     */
    public ElectorateData genElectorateData(ElectionSet electionSet) {
        List<SecretVoterData> secretVoterDataList = new ArrayList<>();
        List<Point> publicVoterDataList = new ArrayList<>();
        List<List<Point>> randomPoints = new ArrayList<>();
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
            PointsAndZeroImages pointsAndZeroImages = polynomialAlgorithms.genPoints(n, k_i);
            SecretVoterData d_i = genSecretVoterData(pointsAndZeroImages.getPoints());
            secretVoterDataList.add(d_i);
            publicVoterDataList.add(getPublicVoterData(d_i.getX(), d_i.getY(), pointsAndZeroImages.getY0s()));
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
     */
    public SecretVoterData genSecretVoterData(List<Point> points) {
        BigInteger x = randomGenerator.randomInZq(q_x);
        BigInteger y = randomGenerator.randomInZq(q_y);
        byte[] F = Arrays.copyOf(hash.hash(points.toArray()), publicParameters.getL_f() / 8);
        byte[][] rc = new byte[points.size()][];

        for (int i = 0; i < points.size(); i++) {
            rc[i] = Arrays.copyOf(hash.hash(points.get(i)), publicParameters.getL_r() / 8);
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
     */
    public Point getPublicVoterData(BigInteger x, BigInteger y, List<BigInteger> yList) {
        BigInteger local_y = y.add(conversion.toInteger(hash.hash(yList.toArray()))).mod(identificationGroup.getQ_circ());
        BigInteger x_circ = identificationGroup.getG_circ().modPow(x, identificationGroup.getP_circ());
        BigInteger y_circ = identificationGroup.getG_circ().modPow(local_y, identificationGroup.getP_circ());

        return new Point(x_circ, y_circ);
    }

    /**
     * Algorithm 5.15: GetPublicCredentials
     *
     * @param D_circ the public voter data generated by each of the authorities
     * @return the combined public voter credentials (one point per voter)
     */
    public List<Point> getPublicCredentials(List<List<Point>> D_circ) {
        Preconditions.checkArgument(D_circ.size() == s,
                String.format("|D_circ| [%d] != s [%d]", D_circ.size(), s));
        return IntStream.range(0, D_circ.get(0).size()).parallel().mapToObj(i -> {
            BigInteger x_circ_i = BigInteger.ONE;
            BigInteger y_circ_i = BigInteger.ONE;
            for (List<Point> d_circ_j : D_circ) {
                Point voterData_ij = d_circ_j.get(i);
                x_circ_i = x_circ_i.multiply(voterData_ij.x).mod(identificationGroup.getP_circ());
                y_circ_i = y_circ_i.multiply(voterData_ij.y).mod(identificationGroup.getP_circ());
            }
            return new Point(x_circ_i, y_circ_i);
        }).collect(Collectors.toList());
    }

}
