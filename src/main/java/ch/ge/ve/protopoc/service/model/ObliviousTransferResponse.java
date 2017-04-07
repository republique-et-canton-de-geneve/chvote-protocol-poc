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
import org.bouncycastle.util.Arrays;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class for an Oblivious Transfer response
 */
public class ObliviousTransferResponse {
    private final List<BigInteger> b;
    private final byte[][] c;
    private final List<BigInteger> d;

    public ObliviousTransferResponse(List<BigInteger> b, byte[][] c, List<BigInteger> d) {
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public List<BigInteger> getB() {
        return ImmutableList.copyOf(b);
    }

    public byte[][] getC() {
        return Arrays.clone(c);
    }

    public List<BigInteger> getD() {
        return ImmutableList.copyOf(d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObliviousTransferResponse that = (ObliviousTransferResponse) o;
        return Objects.equals(b, that.b) &&
                java.util.Arrays.deepEquals(c, that.c) &&
                Objects.equals(d, that.d);
    }

    @Override
    public int hashCode() {
        return Objects.hash(b, c, d);
    }

    @Override
    public String toString() {
        return "ObliviousTransferResponse{" + "b=" + b +
                ", c=" + java.util.Arrays.deepToString(c) +
                ", d=" + d +
                '}';
    }
}
