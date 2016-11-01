package ch.ge.ve.protopoc.service.support;

import java.math.BigInteger;

/**
 * This class handles the conversions between strings, byte arrays and integers
 */
public class Conversion {
    private static final BigInteger BYTE_MULTIPLIER = BigInteger.valueOf(256L);

    public byte[] toByteArray(BigInteger x) {
        return x.toByteArray();
    }

    public BigInteger toInteger(byte[] byteArray) {
        BigInteger integer = BigInteger.ZERO;
        for (byte b : byteArray) {
            integer = integer.multiply(BYTE_MULTIPLIER)
                    // force b to be used as an unsigned value
                    .add(BigInteger.valueOf(b & 0xFF));
        }
        return integer;
    }
}
