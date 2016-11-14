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
}
