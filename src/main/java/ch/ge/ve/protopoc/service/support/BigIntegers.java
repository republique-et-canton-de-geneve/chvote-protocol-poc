package ch.ge.ve.protopoc.service.support;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

/**
 * Commonly used BigInteger values.
 */
public interface BigIntegers {
    BigInteger TWO = BigInteger.valueOf(2L);
    BigInteger THREE = BigInteger.valueOf(3L);
    BigInteger FOUR = BigInteger.valueOf(4L);
    BigInteger FIVE = BigInteger.valueOf(5L);
    BigInteger SIX = BigInteger.valueOf(6L);
    BigInteger SEVEN = BigInteger.valueOf(7L);
    BigInteger EIGHT = BigInteger.valueOf(8L);
    BigInteger NINE = BigInteger.valueOf(9L);
    BigInteger ELEVEN = BigInteger.valueOf(11L);

    /**
     * Get the operator for multiplying two BigIntegers modulo a fixed one
     *
     * @param m the modulus
     * @return an operator on two BigIntegers, multiplying them modulo <tt>m</tt>
     */
    static BinaryOperator<BigInteger> multiplyMod(BigInteger m) {
        return (a, b) -> a.multiply(b).mod(m);
    }
}
