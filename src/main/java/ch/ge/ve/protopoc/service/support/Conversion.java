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

package ch.ge.ve.protopoc.service.support;

import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

/**
 * This class handles the conversions between strings, byte arrays and integers
 */
public class Conversion {
    public static final Charset CONVERSION_CHARSET = Charset.forName("UTF-8");
    private static final BigInteger BYTE_MULTIPLIER = BigInteger.valueOf(256L);

    /**
     * Algorithm 4.3: ToByteArray
     *
     * @param x the integer to be converted
     * @return the byte array corresponding to the integer
     */
    public byte[] toByteArray(BigInteger x) {
        return toByteArray(x, (int) Math.ceil(x.bitLength() / 8.0));
    }

    /**
     * Algorithm 4.4: ToByteArray
     *
     * @param x the integer to be converted
     * @param n the target length (in bytes)
     * @return the converted value, left-padded with <tt>0</tt>s if length is smaller than target, or trucated left if its larger
     */
    public byte[] toByteArray(BigInteger x, int n) {
        Preconditions.checkArgument(x.signum() >= 0, "x must be non-negative");
        Preconditions.checkArgument(n >= (int) Math.ceil(x.bitLength() / 8.0));
        byte[] byteArray = new byte[n];

        BigInteger current = x;
        for (int i = 1; i <= n; i++) {
            byteArray[n - i] = current.byteValue(); // = current.mod(256)
            current = current.shiftRight(8); // current.divide(256)
        }

        return byteArray;
    }

    /**
     * Algorithm 4.5: ToInteger
     *
     * @param byteArray the byte array to be converted
     * @return the corresponding integer (unsigned, non-injective conversion)
     */
    public BigInteger toInteger(byte[] byteArray) {
        BigInteger integer = BigInteger.ZERO;
        for (byte b : byteArray) {
            integer = integer.multiply(BYTE_MULTIPLIER)
                    // force b to be used as an unsigned value
                    .add(BigInteger.valueOf(b & 0xFF));
        }
        return integer;
    }

    /**
     * As described in section 4.2.3 of specification
     *
     * @param s the string to be converted
     * @return the corresponding byte array
     */
    public byte[] toByteArray(String s) {
        return s.getBytes(CONVERSION_CHARSET);
    }

    /**
     * Algorithm 4.6: ToString
     *
     * @param x       the integer to convert
     * @param k       the required String size
     * @param upper_a the alphabet to be used
     * @return a string of length k, using alphabet A, and representing x
     */
    public String toString(BigInteger x, int k, List<Character> upper_a) {
        Preconditions.checkArgument(x.signum() >= 0, "x should be a non-negative integer");
        int alphabetSize = upper_a.size();
        BigInteger N = BigInteger.valueOf(alphabetSize);
        Preconditions.checkArgument(N.pow(k).compareTo(x) >= 0,
                "x is too large to be encoded with k characters of alphabet upper_a");

        StringBuilder sb = new StringBuilder(k);
        BigInteger current = x;
        for (int i = 1; i <= k; i++) {
            BigInteger[] divideAndRemainder = current.divideAndRemainder(N);
            current = divideAndRemainder[0];
            // always insert before the previous character
            sb.insert(0, upper_a.get(divideAndRemainder[1].intValue()));
        }

        return sb.toString();
    }

    /**
     * Algorithm 4.7: ToInteger
     *
     * @param upper_s the string to be converted
     * @param upper_a the alphabet to be used
     * @return the corresponding integer value
     */
    public BigInteger toInteger(String upper_s, List<Character> upper_a) {
        BigInteger upper_n = BigInteger.valueOf(upper_a.size());

        BigInteger x = BigInteger.ZERO;
        for (int i = 0; i < upper_s.length(); i++) {
            int rank_upper_a = upper_a.indexOf(upper_s.charAt(i));
            Preconditions.checkArgument(rank_upper_a >= 0,
                    String.format("character %s not found in alphabet %s", upper_s.charAt(i), upper_a));
            x = x.multiply(upper_n).add(BigInteger.valueOf(rank_upper_a));
        }

        return x;
    }

    /**
     * Algorithm 4.8: ToString
     *
     * @param upper_b the byte array to represent as String
     * @param upper_a the alphabet to use for the conversion
     * @return the corresponding string
     */
    public String toString(byte[] upper_b, List<Character> upper_a) {
        BigInteger x_upper_b = toInteger(upper_b);
        int k = (int) Math.ceil(8.0 * upper_b.length / (Math.log(upper_a.size()) / Math.log(2)));
        return toString(x_upper_b, k, upper_a);
    }
}
