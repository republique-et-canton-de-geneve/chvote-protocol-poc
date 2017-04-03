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

/**
 * Model class for an Oblivious Transfer query
 */
public class ObliviousTransferQuery {
    private final List<BigInteger> bold_a;
    private final List<BigInteger> bold_r;

    public ObliviousTransferQuery(List<BigInteger> bold_a, List<BigInteger> bold_r) {
        this.bold_a = bold_a;
        this.bold_r = bold_r;
    }

    public List<BigInteger> getBold_a() {
        return bold_a;
    }

    public List<BigInteger> getBold_r() {
        return bold_r;
    }
}
