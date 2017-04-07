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

package ch.ge.ve.protopoc.service.support

import spock.lang.Specification

/**
 * Missing javadoc!
 */
class JacobiSymbolTest extends Specification {
    JacobiSymbol jacobiSymbol

    void setup() {
        jacobiSymbol = new JacobiSymbol()
    }

    /**
     * Sample values taken from <a href="https://en.wikipedia.org/wiki/Jacobi_symbol#Table_of_values">Wikipedia's table of values</a>
     * @return
     */
    def "getJacobiSymbol"() {
        expect:
        jacobiSymbol.computeJacobiSymbol(a, n) == i

        where:
        a                       | n                                  | i
        BigInteger.ONE          | BigInteger.ONE                     | 1
        BigIntegers.THREE       | BigIntegers.THREE                  | 0
        BigInteger.TEN          | BigInteger.valueOf(11L)            | -1
        BigInteger.valueOf(14L) | BigInteger.valueOf(51L)            | 1
        BigInteger.valueOf(15L) | BigInteger.valueOf(51L)            | 0
        BigInteger.valueOf(14L) | BigInteger.valueOf(59L)            | -1
        BigInteger.valueOf(15L) | BigInteger.valueOf(59L)            | 1
    }
}
