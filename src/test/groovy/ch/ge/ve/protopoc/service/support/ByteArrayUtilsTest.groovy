package ch.ge.ve.protopoc.service.support

import spock.lang.Specification

/**
 * Missing javadoc!
 */
class ByteArrayUtilsTest extends Specification {

    def "markByteArray should watermark the target array according to spec"() {
        expect:
        ByteArrayUtils.markByteArray(upper_b as byte[], m, m_max) == (result as byte[])

        where:
        upper_b                  | m | m_max || result
        [0x00]                   | 0 | 3     || [0x00]
        [0x00]                   | 1 | 3     || [0x80]
        [0x00]                   | 2 | 3     || [0x08]
        [0x00]                   | 3 | 3     || [0x88]
        [0xFF, 0xFF, 0xFF, 0xFF] | 0 | 3     || [0x7F, 0xFF, 0x7F, 0xFF]
        [0xC1, 0xD2]             | 0 | 3     || [0x41, 0x52]
        [0xC1, 0xD2]             | 1 | 3     || [0xC1, 0x52]
        [0xC1, 0xD2]             | 0 | 15    || [0x41, 0x52]
        [0xCC, 0xDD]             | 0 | 15    || [0x44, 0x55]
    }
}
