package ch.ge.ve.protopoc.service.model;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Model class for the voter
 */
public class Voter {
    private Collection<DomainOfInfluence> allowedDomainsOfInfluence = new ArrayList<>();

    public void addDomainsOfInfluence(DomainOfInfluence... domainsOfInfluence) {
        allowedDomainsOfInfluence.addAll(Arrays.asList(domainsOfInfluence));
    }

    public Collection<DomainOfInfluence> getAllowedDomainsOfInfluence() {
        return ImmutableList.copyOf(allowedDomainsOfInfluence);
    }
}

