/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - chvote-protocol-poc                                                                            -
 - %%                                                                                             -
 - Copyright (C) 2016 - 2017 République et Canton de Genève                                       -
 - %%                                                                                             -
 - This program is free software: you can redistribute it and/or modify                           -
 - it under the terms of the GNU Affero General Public License as published by                    -
 - the Free Software Foundation, either version 3 of the License, or                              -
 - (at your option) any later version.                                                            -
 -                                                                                                -
 - This program is distributed in the hope that it will be useful,                                -
 - but WITHOUT ANY WARRANTY; without even the implied warranty of                                 -
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                                   -
 - GNU General Public License for more details.                                                   -
 -                                                                                                -
 - You should have received a copy of the GNU Affero General Public License                       -
 - along with this program. If not, see <http://www.gnu.org/licenses/>.                           -
 - #L%                                                                                            -
 -------------------------------------------------------------------------------------------------*/

package ch.ge.ve.protopoc.arithmetic;

import ch.ge.ve.protopoc.service.support.JacobiSymbol;
import com.squareup.jnagmp.Gmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * This class provides simplified access to LibGMP if it is loaded, with fallback to vanilla Java BigInteger methods
 */
public class BigIntegerArithmetic {
    private final static Logger log = LoggerFactory.getLogger(BigIntegerArithmetic.class);
    private static final JacobiSymbol jacobiSymbol = new JacobiSymbol();
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

    public static int jacobiSymbol(BigInteger value, BigInteger n) {
        if (gmpLoaded) {
            return Gmp.kronecker(value, n);
        } else {
            return jacobiSymbol.computeJacobiSymbol(value, n);
        }
    }

    public static boolean isGmpLoaded() {
        return gmpLoaded;
    }
}
