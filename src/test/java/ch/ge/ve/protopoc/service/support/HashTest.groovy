package ch.ge.ve.protopoc.service.support

import ch.ge.ve.protopoc.service.model.SecurityParameters
import spock.lang.Specification

import javax.xml.bind.DatatypeConverter
import java.nio.charset.Charset

/**
 * Missing javadoc!
 */
class HashTest extends Specification {
    public static final Charset charset = Charset.forName("UTF-8")
    def Hash hash
    def SecurityParameters securityParameters
    def Conversion conversion = Mock()

    void setup() {
        securityParameters = new SecurityParameters(80, 80, 512, 0.999)
        hash = new Hash("SHA-512", "SUN", securityParameters, conversion)
    }

    def "hash(byte[])"() {
        expect:
        hash.hash(bytes) == digest

        where:
        bytes                    | digest
        "test".getBytes(charset) | DatatypeConverter.parseHexBinary("ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff")
    }

    def "hash(Object[])"() {
        given:
        conversion.toByteArray("test") >> "test".getBytes(charset)
        conversion.toByteArray(_ as BigInteger) >> { args -> (args[0] as BigInteger).toByteArray() }

        expect:
        hash.hash(objects) == digest

        where:
        objects                                                                             | digest
        ["test", BigInteger.valueOf(42L), [0xCC, 0xFF] as byte[]] as Object[]               | [63, -98, 73, -96, 58, 68, 104, -27, -28, -118, 71, -7, 76, -52, -121, 103, -33, -108, -4, 126, 106, 58, 15, -57, -39, 103, 111, 0, 38, 93, 29, 2, 115, -81, -84, -30, -121, -15, -95, -120, 84, 55, 100, 83, 80, 12, -113, -49, 69, 85, -92, 33, 85, -24, -5, -9, 93, -71, -30, -66, 116, -97, -84, 112] as byte[]
        ["test", [BigInteger.valueOf(42L)] as Object[], [0xCC, 0xFF] as byte[]] as Object[] | [63, -98, 73, -96, 58, 68, 104, -27, -28, -118, 71, -7, 76, -52, -121, 103, -33, -108, -4, 126, 106, 58, 15, -57, -39, 103, 111, 0, 38, 93, 29, 2, 115, -81, -84, -30, -121, -15, -95, -120, 84, 55, 100, 83, 80, 12, -113, -49, 69, 85, -92, 33, 85, -24, -5, -9, 93, -71, -30, -66, 116, -97, -84, 112] as byte[]
    }
}
