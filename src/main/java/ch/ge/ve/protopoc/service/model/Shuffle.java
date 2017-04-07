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

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing the result of a shuffle
 */
public class Shuffle {
    private final List<Encryption> bold_e_prime;
    private final List<BigInteger> bold_r_prime;
    private final List<Integer> psy;

    public Shuffle(List<Encryption> bold_e_prime, List<BigInteger> bold_r_prime, List<Integer> psy) {
        this.bold_e_prime = bold_e_prime;
        this.bold_r_prime = bold_r_prime;
        this.psy = psy;
    }

    public List<Encryption> getBold_e_prime() {
        return ImmutableList.copyOf(bold_e_prime);
    }

    public List<BigInteger> getBold_r_prime() {
        return ImmutableList.copyOf(bold_r_prime);
    }

    public List<Integer> getPsy() {
        return psy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shuffle shuffle = (Shuffle) o;
        return Objects.equals(bold_e_prime, shuffle.bold_e_prime) &&
                Objects.equals(bold_r_prime, shuffle.bold_r_prime) &&
                Objects.equals(psy, shuffle.psy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold_e_prime, bold_r_prime, psy);
    }
}
