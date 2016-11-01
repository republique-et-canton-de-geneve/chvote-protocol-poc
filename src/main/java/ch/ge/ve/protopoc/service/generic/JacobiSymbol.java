package ch.ge.ve.protopoc.service.generic;

import java.math.BigInteger;

/**
 * Missing javadoc!
 */
public class JacobiSymbol {

    public int getJacobiSymbol(BigInteger initial_a, BigInteger n) {
        BigInteger a = initial_a.mod(n);
        if (a.equals(BigInteger.ONE) || n.equals(BigInteger.ONE)) {
            return 1;
        }
        if (a.equals(BigInteger.ZERO)) {
            return 0;
        }
        int e = 0;
        BigInteger a_1 = a;
        while (a_1.remainder(BigIntegers.TWO).equals(BigInteger.ZERO)) {
            e++;
            a_1 = a_1.divide(BigIntegers.TWO);
        }
        int s;
        if (e % 2 == 0) {
            s = 1;
        } else {
            BigInteger n_mod_eight = n.mod(BigIntegers.EIGHT);
            if (n_mod_eight.equals(BigInteger.ONE) || n_mod_eight.equals(BigIntegers.SEVEN)) {
                s = 1;
            } else { // n_mod_eight.equals(THREE) || n_mod_eight.equals(FIVE)
                s = -1;
            }
        }
        if (n.mod(BigIntegers.FOUR).equals(BigIntegers.THREE) && a_1.mod(BigIntegers.FOUR).equals(BigIntegers.THREE)) {
            s = -s;
        }
        BigInteger n_1 = n.mod(a_1);
        return s * getJacobiSymbol(n_1, a_1);
    }
}
