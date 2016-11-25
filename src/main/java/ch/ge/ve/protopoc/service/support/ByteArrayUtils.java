package ch.ge.ve.protopoc.service.support;

import com.google.common.base.Preconditions;

/**
 * Missing javadoc!
 */
public class ByteArrayUtils {
    public static byte[] xor(byte[] a, byte[] b) {
        Preconditions.checkArgument(a.length == b.length,
                "The arrays should have the same size. |a| = [" + a.length + "], |b| = [" + b.length + "]");
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] concatenated = new byte[a.length + b.length];
        System.arraycopy(a, 0, concatenated, 0, a.length);
        System.arraycopy(b, 0, concatenated, a.length, b.length);
        return concatenated;
    }
}
