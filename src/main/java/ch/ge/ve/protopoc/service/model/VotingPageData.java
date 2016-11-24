package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Model class containing the data necessary for presenting the voting page to the voter
 */
public class VotingPageData {
    private final List<Integer> selectionCounts;

    public VotingPageData(List<Integer> selectionCounts) {
        this.selectionCounts = selectionCounts;
    }

    public List<Integer> getSelectionCounts() {
        return selectionCounts;
    }
}
