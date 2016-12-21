package ch.ge.ve.protopoc.service.support

import spock.lang.Specification

/**
 * Missing javadoc!
 */
class JacobiSymbolTest extends Specification {
    JacobiSymbol jacobiSymbol

    void setup() {
        jacobiSymbol = new JacobiSymbol()
    }

    /**
     * Sample values taken from <a href="https://en.wikipedia.org/wiki/Jacobi_symbol#Table_of_values">Wikipedia's table of values</a>
     * @return
     */
    def "getJacobiSymbol"() {
        expect:
        jacobiSymbol.computeJacobiSymbol(a, n) == i

        where:
        a                       | n                                  | i
        BigInteger.ONE          | BigInteger.ONE                     | 1
        BigIntegers.THREE       | BigIntegers.THREE                  | 0
        BigInteger.TEN          | BigInteger.valueOf(11L)            | -1
        BigInteger.valueOf(14L) | BigInteger.valueOf(51L)            | 1
        BigInteger.valueOf(15L) | BigInteger.valueOf(51L)            | 0
        BigInteger.valueOf(14L) | BigInteger.valueOf(59L)            | -1
        BigInteger.valueOf(15L) | BigInteger.valueOf(59L)            | 1
    }
}
