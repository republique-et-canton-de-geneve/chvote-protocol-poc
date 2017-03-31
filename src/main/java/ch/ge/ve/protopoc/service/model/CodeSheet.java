package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Contains all the information needed for the printing of a code sheet
 */
public class CodeSheet {
    private final Integer i;
    private final Voter voter;
    private final ElectionSet electionSet;
    private final List<Integer> bold_k;
    private final String upper_x;
    private final String upper_y;
    private final String upper_fc;
    private final List<String> bold_rc;

    public CodeSheet(Integer i, Voter upper_v, ElectionSet electionSet, List<Integer> bold_k, String upper_x, String upper_y, String
            upper_fc, List<String> bold_rc) {
        this.i = i;
        voter = upper_v;
        this.electionSet = electionSet;
        this.bold_k = bold_k;
        this.upper_x = upper_x;
        this.upper_y = upper_y;
        this.upper_fc = upper_fc;
        this.bold_rc = bold_rc;
    }

    public Integer getI() {
        return i;
    }

    public Voter getVoter() {
        return voter;
    }

    public ElectionSet getElectionSet() {
        return electionSet;
    }

    public List<Integer> getBold_k() {
        return bold_k;
    }

    public String getUpper_x() {
        return upper_x;
    }

    public String getUpper_y() {
        return upper_y;
    }

    public String getUpper_fc() {
        return upper_fc;
    }

    public List<String> getBold_rc() {
        return bold_rc;
    }
}
