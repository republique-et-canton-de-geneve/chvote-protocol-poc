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
     * Algorithm 2.1: ToByteArray
     *
     * @param x the integer to be converted
     * @return the byte array corresponding to the integer
     */
    public byte[] toByteArray(BigInteger x) {
        return toByteArray(x, (int) Math.ceil(x.bitLength() / 8.0));
    }

    /**
     * Algorithm 2.2: ToByteArray
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
            BigInteger[] divideAndRemainder = current.divideAndRemainder(BYTE_MULTIPLIER);
            current = divideAndRemainder[0];
            byteArray[n - i] = divideAndRemainder[1].byteValue();
        }

        return byteArray;
    }

    /**
     * Algorithm 2.3: ToInteger
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
     * Algorithm 2.4: ToString
     *
     * @param x the integer to convert
     * @param k the required String size
     * @param A the alphabet to be used
     * @return a string of length k, using alphabet A, and representing x
     */
    public String toString(BigInteger x, int k, List<Character> A) {
        Preconditions.checkArgument(x.signum() >= 0, "x should be a non-negative integer");
        int alphabetSize = A.size();
        BigInteger N = BigInteger.valueOf(alphabetSize);
        Preconditions.checkArgument(N.pow(k).compareTo(x) >= 0,
                "x is too large to be encoded with k characters of alphabet A");

        StringBuilder sb = new StringBuilder(k);
        BigInteger current = x;
        for (int i = 1; i <= k; i++) {
            BigInteger[] divideAndRemainder = current.divideAndRemainder(N);
            current = divideAndRemainder[0];
            // always insert before the previous character
            sb.insert(0, A.get(divideAndRemainder[1].intValue()));
        }

        return sb.toString();
    }

    /**
     * Algorithm 2.5: ToInteger
     *
     * @param S the string to be converted
     * @param A the alphabet to be used
     * @return the corresponding integer value
     */
    public BigInteger toInteger(String S, List<Character> A) {
        BigInteger N = BigInteger.valueOf(A.size());

        BigInteger x = BigInteger.ZERO;
        for (int i = 0; i < S.length(); i++) {
            int rank_A = A.indexOf(S.charAt(i));
            Preconditions.checkArgument(rank_A >= 0,
                    String.format("character %s not found in alphabet %s", S.charAt(i), A));
            x = x.multiply(N).add(BigInteger.valueOf(rank_A));
        }

        return x;
    }

    /**
     * Algorithm 2.6: ToString
     *
     * @param byteArray the byte array to represent as String
     * @return the corresponding string
     */
    public String toString(byte[] byteArray, List<Character> alphabet) {
        BigInteger x = toInteger(byteArray);
        int k = (int) Math.ceil(8.0 * byteArray.length / (Math.log(alphabet.size()) / Math.log(2)));
        return toString(x, k, alphabet);
    }
}
