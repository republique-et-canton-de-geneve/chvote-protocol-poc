package ch.ge.ve.protopoc.service.support;

import com.google.common.base.Preconditions;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * This class handles the conversions between strings, byte arrays and integers
 */
public class Conversion {
    public static final Charset CONVERSION_CHARSET = Charset.forName("UTF-8");
    private static final BigInteger BYTE_MULTIPLIER = BigInteger.valueOf(256L);

    /**
     * Algorithm 2.1
     *
     * @param x the integer to be converted
     * @return the byte array corresponding to the integer
     */
    public byte[] toByteArray(BigInteger x) {
        return toByteArray(x, (int) Math.ceil(x.bitLength() / 8.0));
    }

    /**
     * Algorithm 2.2
     *
     * @param x the integer to be converted
     * @param n the target length (in bytes)
     * @return the converted value, left-padded with <tt>0</tt>s if length is smaller than target, or trucated left if its larger
     */
    public byte[] toByteArray(BigInteger x, int n) {
        Preconditions.checkArgument(x.signum() >= 0, "x must be non-negative");
        byte[] byteArray = new byte[n];

        BigInteger current = x;
        for (int i = 1; i <= n; i++) {
            BigInteger[] divideAndRemainder = current.divideAndRemainder(BYTE_MULTIPLIER);
            current = divideAndRemainder[0];
            byteArray[n - i] = divideAndRemainder[1].byteValue();
        }

        return byteArray;
    }

    /**
     * As described in section 2.2.2 of specification
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
     * As described in section 2.2.3 of specification
     *
     * @param s the string to be converted
     * @return the corresponding byte array
     */
    public byte[] toByteArray(String s) {
        return s.getBytes(CONVERSION_CHARSET);
    }

    /**
     * Description in section 2.2.4 needs more work, temporarily using Base64.
     *
     * @param byteArray the byte array to represent as String
     * @return the corresponding string
     */
    public String toString(byte[] byteArray) {
        return DatatypeConverter.printBase64Binary(byteArray);
    }
}
