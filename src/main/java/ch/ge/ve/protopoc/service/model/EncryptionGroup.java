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

import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * The model class representing the encryption group
 */
public class EncryptionGroup {
    /**
     * Safe prime modulus p
     */
    private final BigInteger p;

    /**
     * Prime order q
     */
    private final BigInteger q;

    /**
     * Generator, independent from h
     */
    private final BigInteger g;

    /**
     * Generator, independent from g
     */
    private final BigInteger h;

    public EncryptionGroup(BigInteger p, BigInteger q, BigInteger g, BigInteger h) {
        // TODO define required certainty levels
        Preconditions.checkArgument(p.isProbablePrime(100));
        Preconditions.checkArgument(q.isProbablePrime(80));
        Preconditions.checkArgument(q.bitLength() == p.bitLength() - 1);
        Preconditions.checkArgument(g.compareTo(BigInteger.ONE) > 0);
        Preconditions.checkArgument(h.compareTo(BigInteger.ONE) > 0);
        this.p = p;
        this.q = q;
        this.g = g;
        this.h = h;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getH() {
        return h;
    }

    @Override
    public String toString() {
        return "EncryptionGroup{" +
                "p=" + p +
                ", q=" + q +
                ", g=" + g +
                ", h=" + h +
                '}';
    }
}
