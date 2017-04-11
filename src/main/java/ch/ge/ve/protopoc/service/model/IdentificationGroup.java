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

import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * The model class representing the identification group
 */
public final class IdentificationGroup {
    private final BigInteger p_hat;
    private final BigInteger q_hat;
    private final BigInteger g_hat;

    public IdentificationGroup(BigInteger p_hat, BigInteger q_hat, BigInteger g_hat) {
        Preconditions.checkArgument(q_hat.bitLength() <= p_hat.bitLength());
        Preconditions.checkArgument(g_hat.compareTo(BigInteger.ONE) != 0);
        Preconditions.checkArgument(p_hat.subtract(BigInteger.ONE).mod(q_hat)
                .compareTo(BigInteger.ZERO) == 0);
        this.p_hat = p_hat;
        this.q_hat = q_hat;
        this.g_hat = g_hat;
    }

    public BigInteger getP_hat() {
        return p_hat;
    }

    public BigInteger getQ_hat() {
        return q_hat;
    }

    public BigInteger getG_hat() {
        return g_hat;
    }
}
