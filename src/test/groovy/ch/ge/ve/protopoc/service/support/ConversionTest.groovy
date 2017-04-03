/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - protocol-poc-back                                                                              -
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
        conversion.toByteArray(s) == (bytes as byte[])

        where:
        s       | bytes
        "Hello" | [(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F]
        "Voilà" | [(byte) 0x56, (byte) 0x6F, (byte) 0x69, (byte) 0x6C, (byte) 0xC3, (byte) 0xA0]
    }

    def "toString(BigInteger, int, List<Character>)"() {
        expect:
        conversion.toString(x, k, A as List<Character>) == s

        where:
        x                          | k | A        || s
        BigInteger.valueOf(0)      | 4 | '0'..'1' || "0000"
        BigInteger.valueOf(0)      | 0 | '0'..'1' || ""
        BigInteger.valueOf(1)      | 4 | '0'..'1' || "0001"
        BigInteger.valueOf(1)      | 1 | '0'..'1' || "1"
        BigInteger.valueOf(2)      | 4 | '0'..'1' || "0010"
        BigInteger.valueOf(2)      | 2 | '0'..'1' || "10"
        BigInteger.valueOf(4)      | 4 | '0'..'1' || "0100"
        BigInteger.valueOf(4)      | 3 | '0'..'1' || "100"
        BigInteger.valueOf(8)      | 4 | '0'..'1' || "1000"
        BigInteger.valueOf(15)     | 4 | '0'..'1' || "1111"
        BigInteger.valueOf(731)    | 4 | 'A'..'Z' || "ABCD"
        BigInteger.valueOf(25)     | 1 | 'A'..'Z' || "Z"
        BigInteger.valueOf(650)    | 2 | 'A'..'Z' || "ZA"
        BigInteger.valueOf(675)    | 2 | 'A'..'Z' || "ZZ"
        BigInteger.valueOf(16900)  | 3 | 'A'..'Z' || "ZAA"
        BigInteger.valueOf(17575)  | 3 | 'A'..'Z' || "ZZZ"
        BigInteger.valueOf(439400) | 4 | 'A'..'Z' || "ZAAA"
        BigInteger.valueOf(456975) | 4 | 'A'..'Z' || "ZZZZ"
    }

    def "toInteger(String, List<Character>"() {
        expect:
        conversion.toInteger(S, (from as Character)..(to as Character)) == x

        where:
        S      | from | to  || x
        ""     | '0'  | '1' || BigInteger.valueOf(0)
        "0001" | '0'  | '1' || BigInteger.valueOf(1)
        "1000" | '0'  | '1' || BigInteger.valueOf(8)
        "1111" | '0'  | '1' || BigInteger.valueOf(15)
        "Z"    | 'A'  | 'Z' || BigInteger.valueOf(25)
        "ZA"   | 'A'  | 'Z' || BigInteger.valueOf(650)
        "ZZ"   | 'A'  | 'Z' || BigInteger.valueOf(675)
        "ZAA"  | 'A'  | 'Z' || BigInteger.valueOf(16900)
        "ZZZ"  | 'A'  | 'Z' || BigInteger.valueOf(17575)
        "ZAAA" | 'A'  | 'Z' || BigInteger.valueOf(439400)
        "ZZZZ" | 'A'  | 'Z' || BigInteger.valueOf(456975)
    }
}
