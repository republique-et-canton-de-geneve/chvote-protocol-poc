package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Contains all the information needed for the printing of a code sheet
 */
public class CodeSheet {
    private final Voter voter;
    private final ElectionSet electionSet;
    private final List<Integer> k_i;
    private final byte[] x_i;
    private final byte[] y_i;
    private final byte[] f_i;
    private final byte[][] rc_i;

    public CodeSheet(Voter v_i, ElectionSet electionSet, List<Integer> k_i, byte[] x_i, byte[] y_i, byte[] f_i, byte[][] rc_i) {
        voter = v_i;
        this.electionSet = electionSet;
        this.k_i = k_i;
        this.x_i = x_i;
        this.y_i = y_i;
        this.f_i = f_i;
        this.rc_i = rc_i;
    }

    public Voter getVoter() {
        return voter;
    }

    public ElectionSet getElectionSet() {
        return electionSet;
    }

    public List<Integer> getK_i() {
        return k_i;
    }

    public byte[] getX_i() {
        return x_i;
    }

    public byte[] getY_i() {
        return y_i;
    }

    public byte[] getF_i() {
        return f_i;
    }

    public byte[][] getRc_i() {
        return rc_i;
    }
}
