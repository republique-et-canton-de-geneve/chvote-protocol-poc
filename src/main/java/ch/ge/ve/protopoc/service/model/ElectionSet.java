package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Missing javadoc!
 */
public class ElectionSet {
    private List<Voter> voters;
    private List<Candidate> candidates;
    private List<Election> elections;

    public boolean isEligible(Voter voter, Election election) {
        return voter.getAllowedDomainsOfInfluence().contains(election.getApplicableDomainofInfluence());
    }
}
