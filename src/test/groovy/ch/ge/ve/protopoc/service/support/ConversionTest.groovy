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

    def "toByteArray(BigInteger)"() {
        expect:
        conversion.toByteArray(x) == (bytes as byte[])

        where:
        x                              | bytes
        BigInteger.valueOf(-128)       | [0x80]
        BigInteger.valueOf(-1)         | [0xFF]
        BigInteger.valueOf(0)          | [0x0]
        BigInteger.valueOf(127)        | [0x7F]
        BigInteger.valueOf(-32_768)    | [0x80, 0x00]
        BigInteger.valueOf(-129)       | [0xFF, 0x7F]
        BigInteger.valueOf(128)        | [0x00, 0x80]
        BigInteger.valueOf(32_767)     | [0x7F, 0xFF]
        BigInteger.valueOf(-8_388_608) | [0x80, 0x00, 0x00]
        BigInteger.valueOf(-32_769)    | [0xFF, 0x7F, 0xFF]
        BigInteger.valueOf(32_768)     | [0x00, 0x80, 0x00]
        BigInteger.valueOf(8_388_607)  | [0x7F, 0xFF, 0xFF]
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
        bytes | s
        [(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F] | "SGVsbG8="
    }
}
