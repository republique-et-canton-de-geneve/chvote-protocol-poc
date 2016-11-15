package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.model.EncryptionPrivateKey
import ch.ge.ve.protopoc.service.model.EncryptionPublicKey
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*

/**
 * Tests for the KeyEstablishment algorithms
 */
class KeyEstablishmentTest extends Specification {
    public static final BigInteger THIRTEEN = BigInteger.valueOf(13)
    def RandomGenerator randomGenerator = Mock()
    def EncryptionGroup encryptionGroup = Mock()

    def KeyEstablishment keyEstablishment

    void setup() {
        keyEstablishment = new KeyEstablishment(randomGenerator)
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
        def pubKeys = [new EncryptionPublicKey(SEVEN, encryptionGroup), new EncryptionPublicKey(THREE, encryptionGroup)] as EncryptionPublicKey[]

        when:
        def publicKey = keyEstablishment.getPublicKey(pubKeys)

        then:
        publicKey.encryptionGroup == encryptionGroup
        publicKey.publicKey == EIGHT // 7 * 3 mod 8
    }
}
