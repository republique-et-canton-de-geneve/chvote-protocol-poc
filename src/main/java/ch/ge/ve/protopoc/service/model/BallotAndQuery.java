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

/**
 * Model class combining a ballot and a OT query
 */
public final class BallotAndQuery {

    private final BigInteger x_hat;
    private final List<BigInteger> bold_a;
    private final BigInteger b;
    private final NonInteractiveZKP pi;

    public BallotAndQuery(BigInteger x_hat, List<BigInteger> bold_a, BigInteger b, NonInteractiveZKP pi) {
        this.x_hat = x_hat;
        this.bold_a = ImmutableList.copyOf(bold_a);
        this.b = b;
        this.pi = pi;
    }

    public BigInteger getX_hat() {
        return x_hat;
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
