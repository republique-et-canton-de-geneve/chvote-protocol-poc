package ch.ge.ve.protopoc.service.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class containing the definition of a set of elections
 */
public class ElectionSet {
    private final List<Voter> voters;
    private final List<Candidate> candidates;
    private final List<Election> elections;

    public ElectionSet(List<Voter> voters, List<Candidate> candidates, List<Election> elections) {
        Preconditions.checkArgument(candidates.size() ==
                elections.stream().map(Election::getNumberOfCandidates).reduce((a, b) -> a + b).orElse(0));
        this.voters = voters;
        this.candidates = candidates;
        this.elections = elections;
    }

    public boolean isEligible(Voter voter, Election election) {
        return voter.getAllowedDomainsOfInfluence().contains(election.getApplicableDomainofInfluence());
    }

    public List<Integer> getBold_n() {
        return elections.stream().map(Election::getNumberOfCandidates).collect(Collectors.toList());
    }

    public List<Voter> getVoters() {
        return ImmutableList.copyOf(voters);
    }

    public List<Candidate> getCandidates() {
        return ImmutableList.copyOf(candidates);
    }

    public List<Election> getElections() {
        return ImmutableList.copyOf(elections);
    }
}
