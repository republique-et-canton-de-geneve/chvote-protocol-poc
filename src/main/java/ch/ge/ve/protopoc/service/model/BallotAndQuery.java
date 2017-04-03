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
 * Model class combining a ballot and a OT query
 */
public class BallotAndQuery {

    private final BigInteger x_circ;
    private final List<BigInteger> bold_a;
    private final BigInteger b;
    private final NonInteractiveZKP pi;

    public BallotAndQuery(BigInteger x_circ, List<BigInteger> bold_a, BigInteger b, NonInteractiveZKP pi) {
        this.x_circ = x_circ;
        this.bold_a = bold_a;
        this.b = b;
        this.pi = pi;
    }

    public BigInteger getX_circ() {
        return x_circ;
    }

    public List<BigInteger> getBold_a() {
        return bold_a;
    }

    public BigInteger getB() {
        return b;
    }

    public NonInteractiveZKP getPi() {
        return pi;
    }
}
