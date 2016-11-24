package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Model class containing the data necessary for presenting the voting page to the voter
 */
public class VotingPageData {
    private final List<Integer> selectionCounts;
    private final List<Integer> candidateCounts;

    public VotingPageData(List<Integer> selectionCounts, List<Integer> candidateCounts) {
        this.selectionCounts = selectionCounts;
        this.candidateCounts = candidateCounts;
    }

    public List<Integer> getSelectionCounts() {
        return selectionCounts;
    }

    public List<Integer> getCandidateCounts() {
        return candidateCounts;
    }
}
