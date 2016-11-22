package ch.ge.ve.protopoc.service.model;

import java.util.Objects;

/**
 * Model class for the entries of the confirmation list held by each authority
 */
public class ConfirmationEntry {
    private final Integer i;
    private final Confirmation gamma;

    public ConfirmationEntry(Integer i, Confirmation gamma) {
        this.i = i;
        this.gamma = gamma;
    }

    public Integer getI() {
        return i;
    }

    public Confirmation getGamma() {
        return gamma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmationEntry that = (ConfirmationEntry) o;
        return Objects.equals(i, that.i) &&
                Objects.equals(gamma, that.gamma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, gamma);
    }

    @Override
    public String toString() {
        return String.format("ConfirmationEntry{i=%d, gamma=%s}", i, gamma);
    }
}
