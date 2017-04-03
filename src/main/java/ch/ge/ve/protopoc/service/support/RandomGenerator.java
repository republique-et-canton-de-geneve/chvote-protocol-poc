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

import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.security.SecureRandom;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;

/**
 * This class
 */
public class RandomGenerator {
    private static final int MAX_ITERATIONS = Byte.MAX_VALUE - Byte.MIN_VALUE;
    private final SecureRandom secureRandom;

    public RandomGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Generate a random integer in the given range, taken from a uniform random distribution
     *
     * @param from the start of the range, inclusive
     * @param to   the end of the range, inclusive
     * @return a random integer within range
     */
    public int randomIntInRange(int from, int to) {
        Preconditions.checkArgument(from <= to, "The lowerbound must be less or equal to the upperbound");
        if (from == to) return from;
        return secureRandom.nextInt(to - from) + from;
    }

    /**
     * @param upperbound exclusive upperbound
     * @return a random BigInteger in range [0, upperbound)
     */
    public BigInteger randomBigInteger(BigInteger upperbound) {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            BigInteger x = new BigInteger(upperbound.bitLength(), secureRandom);
            if (x.compareTo(upperbound) < 0) {
                return x;
            }
        }

        // If we fail to get a value within range for MAX_ITERATIONS, get a value with lower bitCount
        return new BigInteger(upperbound.bitLength() - 1, secureRandom);
    }

    /**
     * Draw at random from Z_q
     *
     * @param q the exclusive upperbound to draw from
     * @return an element picked at random from a uniform distribution of Z_q
     */
    public BigInteger randomInZq(BigInteger q) {
        return randomBigInteger(q.subtract(BigInteger.ONE));
    }

    /**
     * Draw an element at random from the group G_q
     *
     * @param encryptionGroup the encryption group to draw from
     * @return an element picked at random
     */
    public BigInteger randomInGq(EncryptionGroup encryptionGroup) {
        BigInteger x = randomInZq(encryptionGroup.getQ());
        return modExp(encryptionGroup.getG(), x, encryptionGroup.getP());
    }
}
