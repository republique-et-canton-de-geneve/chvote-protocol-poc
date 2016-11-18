package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.model.polynomial.Point;

import java.util.List;

/**
 * Model class holding data for the whole electorate
 */
public class ElectorateData {
    private final List<SecretVoterData> d;
    private final List<Point> d_circ;
    private final List<List<Point>> P;
    private final List<List<Integer>> K;

    public ElectorateData(List<SecretVoterData> secretVoterDataList, List<Point> publicVoterDataList, List<List<Point>> randomPoints, List<List<Integer>> allowedSelections) {
        this.d = secretVoterDataList;
        this.d_circ = publicVoterDataList;
        this.P = randomPoints;
        this.K = allowedSelections;
    }

    public List<SecretVoterData> getD() {
        return d;
    }

    public List<Point> getD_circ() {
        return d_circ;
    }

    public List<List<Point>> getP() {
        return P;
    }

    public List<List<Integer>> getK() {
        return K;
    }
}
