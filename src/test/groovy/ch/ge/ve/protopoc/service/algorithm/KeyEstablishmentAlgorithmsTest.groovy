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

package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.model.EncryptionPrivateKey
import ch.ge.ve.protopoc.service.model.EncryptionPublicKey
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*

/**
 * Tests on the algorithms used during key establishment
 */
class KeyEstablishmentAlgorithmsTest extends Specification {
    RandomGenerator randomGenerator = Mock()
    EncryptionGroup encryptionGroup = Mock()

    KeyEstablishmentAlgorithms keyEstablishment

    void setup() {
        keyEstablishment = new KeyEstablishmentAlgorithms(randomGenerator)
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE
        encryptionGroup.g >> THREE
        encryptionGroup.h >> FIVE
    }

    def "generateKeyPair"() {
        when:
        def keyPair = keyEstablishment.generateKeyPair(encryptionGroup)

        then:
        1 * randomGenerator.randomInZq(_) >> THREE

        (keyPair.private as EncryptionPrivateKey).privateKey == THREE
        (keyPair.public as EncryptionPublicKey).publicKey == FIVE // 3 ^ 3 mod 11
    }

    def "getPublicKey"() {
        def pubKeys = [new EncryptionPublicKey(FIVE, encryptionGroup), new EncryptionPublicKey(THREE, encryptionGroup)]

        when:
        def publicKey = keyEstablishment.getPublicKey(pubKeys)

        then:
        publicKey.encryptionGroup == encryptionGroup
        publicKey.publicKey == FOUR // 5 * 3 mod 11
    }
}
