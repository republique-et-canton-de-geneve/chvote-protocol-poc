package ch.ge.ve.protopoc.service.model;

/**
 * Missing javadoc!
 */
public class Election {
    private final int numberOfCandidates;
    private final int numberOfSelections;
    private final DomainOfInfluence applicableDomainofInfluence;

    public Election(int numberOfCandidates, int numberOfSelections, DomainOfInfluence applicableDomainofInfluence) {
        this.numberOfCandidates = numberOfCandidates;
        this.numberOfSelections = numberOfSelections;
        this.applicableDomainofInfluence = applicableDomainofInfluence;
    }

    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    public int getNumberOfSelections() {
        return numberOfSelections;
    }

    public DomainOfInfluence getApplicableDomainofInfluence() {
        return applicableDomainofInfluence;
    }
}
