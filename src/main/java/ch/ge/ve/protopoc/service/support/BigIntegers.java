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

package ch.ge.ve.protopoc.service.support;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

/**
 * Commonly used BigInteger values.
 */
public interface BigIntegers {
    BigInteger TWO = BigInteger.valueOf(2L);
    BigInteger THREE = BigInteger.valueOf(3L);
    BigInteger FOUR = BigInteger.valueOf(4L);
    BigInteger FIVE = BigInteger.valueOf(5L);
    BigInteger SIX = BigInteger.valueOf(6L);
    BigInteger SEVEN = BigInteger.valueOf(7L);
    BigInteger EIGHT = BigInteger.valueOf(8L);
    BigInteger NINE = BigInteger.valueOf(9L);
    BigInteger ELEVEN = BigInteger.valueOf(11L);

    /**
     * Get the operator for multiplying two BigIntegers modulo a fixed one
     *
     * @param m the modulus
     * @return an operator on two BigIntegers, multiplying them modulo <tt>m</tt>
     */
    static BinaryOperator<BigInteger> multiplyMod(BigInteger m) {
        return (a, b) -> a.multiply(b).mod(m);
    }
}
