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
