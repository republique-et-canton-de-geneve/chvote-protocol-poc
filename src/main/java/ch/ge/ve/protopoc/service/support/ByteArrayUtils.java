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

import java.util.Arrays;

/**
 * This utility class provides static, thread-safe methods on byte arrays.
 * <p>
 * <p>All methods work on copy of the given arrays, to ensure consistent behavior, even if the given arrays are
 * modified during execution</p>
 */
public class ByteArrayUtils {
    public static byte[] xor(byte[] a, byte[] b) {
        byte[] local_a = Arrays.copyOf(a, a.length);
        byte[] local_b = Arrays.copyOf(b, b.length);
        Preconditions.checkArgument(local_a.length == local_b.length,
                "The arrays should have the same size. |a| = [" + local_a.length + "], |b| = [" + local_b.length + "]");
        byte[] result = new byte[local_a.length];
        for (int i = 0; i < local_a.length; i++) {
            result[i] = (byte) (local_a[i] ^ local_b[i]);
        }
        return result;
    }

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] local_a = Arrays.copyOf(a, a.length);
        byte[] local_b = Arrays.copyOf(b, b.length);
        byte[] concatenated = new byte[local_a.length + local_b.length];
        System.arraycopy(local_a, 0, concatenated, 0, local_a.length);
        System.arraycopy(local_b, 0, concatenated, local_a.length, local_b.length);
        return concatenated;
    }

    /**
     * Truncate function, as defined in section 4.1 <strong>Byte Arrays</strong>
     *
     * @param a      the array to be truncate
     * @param length the requested length
     * @return a copy of the array, truncated to the requested length
     */
    public static byte[] truncate(byte[] a, int length) {
        byte[] local_a = Arrays.copyOf(a, a.length);
        Preconditions.checkArgument(local_a.length >= length,
                "The given array is small than the requested length");
        return Arrays.copyOfRange(local_a, 0, length);
    }

    /**
     * Extract function, as defined in section 4.1 <strong>Byte Arrays</strong>
     *
     * @param a     the array from which to extract the values
     * @param start the starting position (inclusive)
     * @param end   the ending position (exclusive)
     * @return a copy of the range of the array between start (incl.) and end (excl.)
     */
    public static byte[] extract(byte[] a, int start, int end) {
        byte[] local_a = Arrays.copyOf(a, a.length);
        Preconditions.checkArgument(start >= 0,
                "Start index must be non-negative");
        Preconditions.checkArgument(start < end,
                "The starting position must be strictly smaller than the ending position");
        Preconditions.checkArgument(local_a.length >= end,
                "The ending position may not be larger than the array's length");
        return Arrays.copyOfRange(local_a, start, end);
    }

    /**
     * Algorithm 4.1: MarkByteArray
     * <p>
     * Adds an integer watermark <tt>m</tt> to the bits of a given byte array.
     * The bits of the watermark are spread equally across the bits of the byte array.
     * </p>
     *
     * @param upper_b the byte array to watermark
     * @param m       the watermark: 0 <= m <= m_max
     * @param m_max   the maximal watermark: ||m_max|| <= 8 * |upper_b|
     * @return the watermarked byte array
     */
    public static byte[] markByteArray(byte[] upper_b, int m, int m_max) {
        Preconditions.checkArgument(0 <= m,
                "m must be non-negative");
        Preconditions.checkArgument(m <= m_max,
                "m must be smaller or equal to m_max");
        Preconditions.checkArgument(bitLength(m_max) <= 8 * upper_b.length,
                "m_max must be smaller or equal to the number of bits in upper_b");
        int l = bitLength(m_max);
        double s = ((double) (8 * upper_b.length)) / ((double) l);
        byte[] local_upper_b = new byte[upper_b.length];
        System.arraycopy(upper_b, 0, local_upper_b, 0, upper_b.length);
        for (int i = 0; i <= l - 1; i++) {
            local_upper_b = ByteArrayUtils.setBit(local_upper_b, (int) Math.floor(i * s), m % 2 == 1);
            m = m / 2;
        }
        return local_upper_b;
    }

    /**
     * Algorithm 4.2: SetBit
     * <p>
     * Sets the i-th bit of a byte array B to b \in (0,1)
     * </p>
     *
     * @param upper_b the byte array
     * @param i       the position of the bit that must be set
     * @param b       the value which the bit will take
     * @return the modified byte array
     */
    private static byte[] setBit(byte[] upper_b, int i, boolean b) {
        Preconditions.checkArgument(0 <= i, "i must be non-negative");
        Preconditions.checkArgument(i <= 8 * upper_b.length, "i must be smaller or equal to the number of bits in " +
                "upper_b");
        byte[] local_upper_b = new byte[upper_b.length];
        System.arraycopy(upper_b, 0, local_upper_b, 0, upper_b.length);
        int j = i / 8;
        int x = (int) Math.pow(2, i % 8);
        if (!b) {
            local_upper_b[j] = (byte) ((int) local_upper_b[j] & (0xFF - x));
        } else {
            local_upper_b[j] = (byte) ((int) local_upper_b[j] | x);
        }
        return local_upper_b;
    }

    private static int bitLength(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }
}
