package ch.ge.ve.protopoc.arithmetic;

import com.squareup.jnagmp.Gmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * This class provides simplified access to LibGMP if it is loaded, with fallback to vanilla Java BigInteger methods
 */
public class BigIntegerArithmetic {
    private final static Logger log = LoggerFactory.getLogger(BigIntegerArithmetic.class);
    private static boolean gmpLoaded = false;

    static {
        try {
            Gmp.checkLoaded();
            gmpLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            log.error("LibGMP is not available, computations will be much slower");
        }
    }

    public static BigInteger modExp(BigInteger base, BigInteger exponent, BigInteger modulus) {
        if (gmpLoaded) {
            if (exponent.signum() < 0) {
                return Gmp.modPowSecure(modInverse(base, modulus), exponent.negate(), modulus);
            } else {
                return Gmp.modPowSecure(base, exponent, modulus);
            }
        } else {
            return base.modPow(exponent, modulus);
        }
    }

    public static BigInteger modInverse(BigInteger value, BigInteger modulus) {
        if (gmpLoaded) {
            return Gmp.modInverse(value, modulus);
        } else {
            return value.modInverse(modulus);
        }
    }

    public static boolean isGmpLoaded() {
        return gmpLoaded;
    }
}
