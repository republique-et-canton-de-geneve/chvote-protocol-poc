package ch.ge.ve.protopoc.service.model;

import java.util.Objects;

/**
 * Missing javadoc!
 */
public class Candidate {
    private final String candidateDescription;

    public Candidate(String candidateDescription) {
        this.candidateDescription = candidateDescription;
    }

    public String getCandidateDescription() {
        return candidateDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidate candidate = (Candidate) o;
        return Objects.equals(candidateDescription, candidate.candidateDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateDescription);
    }
}
