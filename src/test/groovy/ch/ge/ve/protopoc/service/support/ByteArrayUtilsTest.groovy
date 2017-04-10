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

package ch.ge.ve.protopoc.service.support

import spock.lang.Specification

/**
 * This test class holds the tests for the utility methods defined in ByteArrayUtils
 */
class ByteArrayUtilsTest extends Specification {

    def "markByteArray should watermark the target array according to spec"() {
        expect:
        ByteArrayUtils.markByteArray(upper_b as byte[], m, m_max) == (result as byte[])

        where:
        upper_b                  | m | m_max || result
        [0x00]                   | 0 | 3     || [0x00] // (0x00 & 0xFE) & 0xEF
        [0x00]                   | 1 | 3     || [0x01] // (0x00 | 0x01) & 0xEF
        [0x00]                   | 2 | 3     || [0x10] // (0x00 & 0xFE) | 0x10
        [0x00]                   | 3 | 3     || [0x11] // (0x00 | 0x01) | 0x10
        [0xFF, 0xFF, 0xFF, 0xFF] | 0 | 3     || [0xFE, 0xFF, 0xFE, 0xFF]
        [0xC1, 0xD2]             | 0 | 3     || [0xC0, 0xD2]
        [0xC1, 0xD2]             | 1 | 3     || [0xC1, 0xD2]
        [0xC1, 0xD2]             | 0 | 15    || [0xC0, 0xC2]
        [0xCC, 0xDD]             | 0 | 15    || [0xCC, 0xCC]
        [0xE3, 0xF4]             | 1 | 3     || [0xE3, 0xF4]
    }
}
