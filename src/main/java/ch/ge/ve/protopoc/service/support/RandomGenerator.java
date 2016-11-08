package ch.ge.ve.protopoc.service.support;

import java.math.BigInteger;
import java.security.SecureRandom;

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
     *
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
}
