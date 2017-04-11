/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - chvote-protocol-poc                                                                            -
 - %%                                                                                             -
 - Copyright (C) 2016 - 2017 République et Canton de Genève                                       -
 - %%                                                                                             -
 - This program is free software: you can redistribute it and/or modify                           -
 - it under the terms of the GNU Affero General Public License as published by                    -
 - the Free Software Foundation, either version 3 of the License, or                              -
 - (at your option) any later version.                                                            -
 -                                                                                                -
 - This program is distributed in the hope that it will be useful,                                -
 - but WITHOUT ANY WARRANTY; without even the implied warranty of                                 -
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                                   -
 - GNU General Public License for more details.                                                   -
 -                                                                                                -
 - You should have received a copy of the GNU Affero General Public License                       -
 - along with this program. If not, see <http://www.gnu.org/licenses/>.                           -
 - #L%                                                                                            -
 -------------------------------------------------------------------------------------------------*/

package ch.ge.ve.protopoc.service.model;

import java.util.Objects;

/**
 * Model class for the entries of the confirmation list held by each authority
 */
public final class ConfirmationEntry {
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
