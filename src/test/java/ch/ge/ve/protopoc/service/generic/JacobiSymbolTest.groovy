package ch.ge.ve.protopoc.service.generic

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Missing javadoc!
 */
class JacobiSymbolTest extends Specification {
    def JacobiSymbol jacobiSymbol;

    void setup() {
        jacobiSymbol = new JacobiSymbol()
    }

    /**
     * Sample values taken from <a href="https://en.wikipedia.org/wiki/Jacobi_symbol#Table_of_values">Wikipedia's table of values</a>
     * @return
     */
    def "getJacobiSymbol"() {
        expect:
        jacobiSymbol.getJacobiSymbol(a, n) == i

        where:
        a                 | n                                  | i
        BigInteger.ONE    | BigInteger.ONE                     | 1
        BigIntegers.THREE | BigIntegers.THREE                  | 0
        BigInteger.TEN    | BigInteger.TEN.add(BigInteger.ONE) | -1
    }
}
