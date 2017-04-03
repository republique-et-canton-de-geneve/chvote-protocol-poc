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

/**
 * This class exposes a method to compute the jacobi symbol of a pair of numbers.
 */
public class JacobiSymbol {
    /**
     * Compute the jacobi symbol <code>(a/n)</code>, as described in:
     * <a href="http://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.186-4.pdf">Digital signature standard (DSS). FIPS PUB 186-4, National Institute of Standards and
     Technology (NIST), 2013.</a>, pp. 76-77
     * @param initial_a the starting value of a
     * @param n the value of n
     * @return the computed jacobi symbol
     */
    public int computeJacobiSymbol(BigInteger initial_a, BigInteger n) {
        // Step 1: a = a mod n
        BigInteger a = initial_a.mod(n);
        // Step 2: if a = 1 or n = 1 return 1
        if (a.equals(BigInteger.ONE) || n.equals(BigInteger.ONE)) {
            return 1;
        }
        // Step 3: if a = 0 return 0
        if (a.equals(BigInteger.ZERO)) {
            return 0;
        }
        // Step 4: define e and a_1 such that a = 2^e * a_1 where a_1 is odd
        int e = 0;
        BigInteger a_1 = a;
        while (a_1.remainder(BigIntegers.TWO).equals(BigInteger.ZERO)) {
            e++;
            a_1 = a_1.divide(BigIntegers.TWO);
        }
        // Step 5: if e is even, then s = 1;
        //          else if n mod 8 = 1 or n mod 8 = 7, then s = 1
        //          else if n mod 8 = 3 or n mod 8 = 5, then s = -1
        int s;
        if (e % 2 == 0) {
            s = 1;
        } else {
            BigInteger n_mod_eight = n.mod(BigIntegers.EIGHT);
            if (n_mod_eight.equals(BigInteger.ONE) || n_mod_eight.equals(BigIntegers.SEVEN)) {
                s = 1;
            } else { // n_mod_eight.equals(THREE) || n_mod_eight.equals(FIVE)
                s = -1;
            }
        }
        // Step 6: if n mod 4 = 3 and a_1 mod 4 = 3, then s = -s
        if (n.mod(BigIntegers.FOUR).equals(BigIntegers.THREE) && a_1.mod(BigIntegers.FOUR).equals(BigIntegers.THREE)) {
            s = -s;
        }
        // Step 7: n_1 = n mod a_1
        BigInteger n_1 = n.mod(a_1);
        // Step 8: return s * JacobiSymbol(n_1, a_1)
        return s * computeJacobiSymbol(n_1, a_1);
    }
}
