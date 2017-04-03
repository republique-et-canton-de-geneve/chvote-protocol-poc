/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - protocol-poc-back                                                                              -
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

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing the tuple &lt;beta, bold_r&gt;, returned by Algorithm 5.28
 */
public class ObliviousTransferResponseAndRand {
    private final ObliviousTransferResponse beta;
    private final List<BigInteger> bold_r;

    public ObliviousTransferResponseAndRand(ObliviousTransferResponse beta, List<BigInteger> bold_r) {
        this.beta = beta;
        this.bold_r = bold_r;
    }

    public ObliviousTransferResponse getBeta() {
        return beta;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObliviousTransferResponseAndRand that = (ObliviousTransferResponseAndRand) o;
        return Objects.equals(beta, that.beta) &&
                Objects.equals(bold_r, that.bold_r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beta, bold_r);
    }

    @Override
    public String toString() {
        return "ObliviousTransferResponseAndRand{" + "beta=" + beta +
                ", bold_r=" + bold_r +
                '}';
    }
}
