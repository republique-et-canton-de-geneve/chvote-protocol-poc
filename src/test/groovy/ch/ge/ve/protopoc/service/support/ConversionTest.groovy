package ch.ge.ve.protopoc.service.support

import spock.lang.Specification

/**
 * Missing javadoc!
 */
class ConversionTest extends Specification {
    Conversion conversion

    void setup() {
        conversion = new Conversion()
    }

    def "toByteArray(BigInteger, int)"() {
        expect:
        conversion.toByteArray(x, n) == (bytes as byte[])

        where:
        x                       | n || bytes
        BigInteger.valueOf(0)   | 0 || []
        BigInteger.valueOf(0)   | 1 || [0x00]
        BigInteger.valueOf(0)   | 2 || [0x00, 0x00]
        BigInteger.valueOf(0)   | 3 || [0x00, 0x00, 0x00]
        BigInteger.valueOf(255) | 1 || [0xFF]
        BigInteger.valueOf(255) | 2 || [0x00, 0xFF]
        BigInteger.valueOf(255) | 3 || [0x00, 0x00, 0xFF]
        BigInteger.valueOf(256) | 2 || [0x01, 0x00]
        BigInteger.valueOf(256) | 3 || [0x00, 0x01, 0x00]
        BigInteger.valueOf(256) | 4 || [0x00, 0x00, 0x01, 0x00]

    }

    def "toByteArray(BigInteger)"() {
        expect:
        conversion.toByteArray(x) == (bytes as byte[])

        where:
        x                              || bytes
        BigInteger.valueOf(0)          || []
        BigInteger.valueOf(1)          || [0x1]
        BigInteger.valueOf(255)        || [0xFF]
        BigInteger.valueOf(256)        || [0x01, 0x00]
        BigInteger.valueOf(65_535)     || [0xFF, 0xFF]
        BigInteger.valueOf(65_536)     || [0x01, 0x00, 0x00]
        BigInteger.valueOf(16_777_215) || [0xFF, 0xFF, 0xFF]
        BigInteger.valueOf(16_777_216) || [0x01, 0x00, 0x00, 0x00]
    }

    def "toInteger"() {
        expect:
        conversion.toInteger(conversion.toByteArray(x)) == x

        where:
        x << [BigInteger.ONE,
              BigInteger.valueOf(128),
              BigInteger.valueOf(12_938_765_425_438L)]
    }

    def "toByteArray(String)"() {
        expect:
        conversion.toByteArray(s) == bytes

        where:
        s       | bytes
        "Hello" | [(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F]
        "Voilà" | [(byte) 0x56, (byte) 0x6F, (byte) 0x69, (byte) 0x6C, (byte) 0xC3, (byte) 0xA0]
    }

    def "toString(byte[])"() {
        expect:
        conversion.toString(bytes as byte[]) == s

        where:
        bytes                                                             | s
        [(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F] | "SGVsbG8="
    }
}
