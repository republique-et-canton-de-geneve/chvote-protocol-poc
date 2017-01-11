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
}
