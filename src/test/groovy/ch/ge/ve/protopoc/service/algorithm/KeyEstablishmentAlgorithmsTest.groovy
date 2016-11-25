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
    public static final BigInteger THIRTEEN = BigInteger.valueOf(13)
    RandomGenerator randomGenerator = Mock()
    EncryptionGroup encryptionGroup = Mock()

    KeyEstablishmentAlgorithms keyEstablishment

    void setup() {
        keyEstablishment = new KeyEstablishmentAlgorithms(randomGenerator)
        encryptionGroup.p >> THIRTEEN
        encryptionGroup.q >> SEVEN
        encryptionGroup.g >> THREE
        encryptionGroup.h >> FIVE
    }

    def "generateKeyPair"() {
        when:
        def keyPair = keyEstablishment.generateKeyPair(encryptionGroup)

        then:
        1 * randomGenerator.randomInZq(_) >> FIVE

        (keyPair.private as EncryptionPrivateKey).privateKey == FIVE // 19 mod 7 = 5
        (keyPair.public as EncryptionPublicKey).publicKey == BigInteger.valueOf(9L) // 3 ^ 5 mod 13
    }

    def "getPublicKey"() {
        def pubKeys = [new EncryptionPublicKey(SEVEN, encryptionGroup), new EncryptionPublicKey(THREE, encryptionGroup)]

        when:
        def publicKey = keyEstablishment.getPublicKey(pubKeys)

        then:
        publicKey.encryptionGroup == encryptionGroup
        publicKey.publicKey == EIGHT // 7 * 3 mod 8
    }
}
