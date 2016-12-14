package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.DecryptionProof
import ch.ge.ve.protopoc.service.model.Encryption
import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.model.PublicParameters
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the algorithms performed during the tallying phase
 */
class TallyingAuthoritiesAlgorithmTest extends Specification {
    // Primary Mocks
    PublicParameters publicParameters = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()

    // Secondary Mocks
    EncryptionGroup encryptionGroup = Mock()

    // Class under test
    TallyingAuthoritiesAlgorithm tallyingAuthoritiesAlgorithm

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        publicParameters.s >> 2

        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE // G_q = (1, 3, 4, 5, 9)
        encryptionGroup.g >> THREE

        tallyingAuthoritiesAlgorithm = new TallyingAuthoritiesAlgorithm(publicParameters, generalAlgorithms)
    }

    def "checkDecryptionProofs should validate the proofs for all authorities"() {
        given:
        def bold_pi_prime = [
                new DecryptionProof([NINE, THREE, NINE, FIVE, FOUR], ZERO),
                new DecryptionProof([FOUR, NINE, FOUR, THREE, FIVE], ONE)
        ]
        def bold_pk = [FIVE, THREE]
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def bold_B_prime = [
                [FOUR, FIVE, NINE, THREE],
                [FIVE, THREE, FOUR, NINE]
        ]
        generalAlgorithms.getNIZKPChallenge(*_) >>> [ONE, TWO]

        expect:
        tallyingAuthoritiesAlgorithm.checkDecryptionProofs(bold_pi_prime, bold_pk, bold_e, bold_B_prime)
    }

    def "checkDecryptionProof should correctly validate an authority's partial decryption proof"() {
        given:
        def pi_prime = new DecryptionProof([NINE, THREE, NINE, FIVE, FOUR], ZERO)
        def pk_j = FIVE
        def bold_e = [
                new Encryption(ONE, FIVE),
                new Encryption(NINE, THREE),
                new Encryption(FOUR, FOUR),
                new Encryption(FIVE, NINE)
        ]
        def bold_b_prime = [FOUR, FIVE, NINE, THREE]
        generalAlgorithms.getNIZKPChallenge(*_) >> ONE

        expect:
        tallyingAuthoritiesAlgorithm.checkDecryptionProof(pi_prime, pk_j, bold_e, bold_b_prime)
    }
}
