package ch.ge.ve.protopoc.service.model;

import java.util.Objects;

/**
 * Model class for the domain of influence (as per eCH-0155)
 */
public class DomainOfInfluence {
    private final String identifier;

    public DomainOfInfluence(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainOfInfluence that = (DomainOfInfluence) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return String.format("DomainOfInfluence{identifier='%s'}", identifier);
    }
}
