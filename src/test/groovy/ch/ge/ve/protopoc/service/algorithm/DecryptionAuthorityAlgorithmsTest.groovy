package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the algorithms performed during the decryption phase
 */
class DecryptionAuthorityAlgorithmsTest extends Specification {
    // Primary Mocks
    PublicParameters publicParameters = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()
    MixingAuthorityAlgorithms mixingAuthorityAlgorithms = Mock()
    RandomGenerator randomGenerator = Mock()

    // Secondary Mocks
    EncryptionGroup encryptionGroup = Mock()

    // Class under test
    DecryptionAuthorityAlgorithms decryptionAuthorityAlgorithms

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        publicParameters.s >> 4

        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE

        decryptionAuthorityAlgorithms = new DecryptionAuthorityAlgorithms(publicParameters, generalAlgorithms,
                mixingAuthorityAlgorithms, randomGenerator)

    }

    def "checkShuffleProofs should check the shuffles performed by the other authorities"() {
        ShuffleProof pi_0 = Mock()
        ShuffleProof pi_1 = Mock()
        ShuffleProof pi_2 = Mock()
        ShuffleProof pi_3 = Mock()
        def bold_pi = [pi_0, pi_1, pi_2, pi_3]

        List<Encryption> e_0 = Mock()
        List<Encryption> e_1 = Mock()
        List<Encryption> e_2 = Mock()
        List<Encryption> e_3 = Mock()
        List<Encryption> e_4 = Mock()
        def bold_E = [e_1, e_2, e_3, e_4]

        EncryptionPublicKey pk = Mock()

        int j = 2

        when:
        def result = decryptionAuthorityAlgorithms.checkShuffleProofs(bold_pi, e_0, bold_E, pk, j)

        then:
        (1 * mixingAuthorityAlgorithms.checkShuffleProof(pi_0, e_0, e_1, pk)) >> true
        (1 * mixingAuthorityAlgorithms.checkShuffleProof(pi_1, e_1, e_2, pk)) >> true
        0 * mixingAuthorityAlgorithms.checkShuffleProof(pi_2, e_2, e_3, pk)
        (1 * mixingAuthorityAlgorithms.checkShuffleProof(pi_3, e_3, e_4, pk)) >> true
        result // == true implied
    }

    def "checkShuffleProofs should fail lazily if any of the checks fails"() {
        ShuffleProof pi_0 = Mock()
        ShuffleProof pi_1 = Mock()
        ShuffleProof pi_2 = Mock()
        ShuffleProof pi_3 = Mock()
        def bold_pi = [pi_0, pi_1, pi_2, pi_3]

        List<Encryption> e_0 = Mock()
        List<Encryption> e_1 = Mock()
        List<Encryption> e_2 = Mock()
        List<Encryption> e_3 = Mock()
        List<Encryption> e_4 = Mock()
        def bold_E = [e_1, e_2, e_3, e_4]

        EncryptionPublicKey pk = Mock()

        int j = 2

        when:
        result = decryptionAuthorityAlgorithms.checkShuffleProofs(bold_pi, e_0, bold_E, pk, j)

        then:
        mixingAuthorityAlgorithms.checkShuffleProof(*_) >>> [checkResults]

        where:
        checkResults        || result
        [false, true, true] || false
        [true, false, true] || false
        [true, true, false] || false
    }

    def "getPartialDecryptions should perform partial decryptions on provided encryptions"() {
        given:
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def sk_j = THREE

        expect:
        decryptionAuthorityAlgorithms.getPartialDecryptions(bold_e, sk_j) == [FOUR, FIVE, NINE, THREE]
    }

    def "genDecryptionProof should generate a valid decryption proof"() {
        given:
        def sk_j = THREE
        def pk_j = FIVE
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def bold_b_prime = [FOUR, FIVE, NINE, THREE]
        randomGenerator.randomInZq(FIVE) >> TWO
        generalAlgorithms.getNIZKPChallenge(*_) >> ONE

        expect:
        decryptionAuthorityAlgorithms.genDecryptionProof(sk_j, pk_j, bold_e, bold_b_prime) ==
                new DecryptionProof([NINE, THREE, NINE, FIVE, FOUR], ZERO)
    }
}
