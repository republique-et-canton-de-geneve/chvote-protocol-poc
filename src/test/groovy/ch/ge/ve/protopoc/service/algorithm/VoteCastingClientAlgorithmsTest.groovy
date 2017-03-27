package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.model.polynomial.Point
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests for the Vote Casting algorithms
 */
class VoteCastingClientAlgorithmsTest extends Specification {
    Hash hash = Mock()
    PublicParameters publicParameters = Mock()
    EncryptionGroup encryptionGroup = Mock()
    IdentificationGroup identificationGroup = Mock()
    SecurityParameters securityParameters = Mock()
    PrimeField primeField = Mock()
    RandomGenerator randomGenerator = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()

    VoteCastingClientAlgorithms voteCastingClient

    void setup() {
        def defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray()

        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> ELEVEN
        encryptionGroup.q >> FIVE
        encryptionGroup.g >> THREE
        publicParameters.identificationGroup >> identificationGroup
        identificationGroup.p_circ >> ELEVEN
        identificationGroup.q_circ >> FIVE
        identificationGroup.g_circ >> THREE
        publicParameters.primeField >> primeField
        primeField.p_prime >> FIVE
        publicParameters.securityParameters >> securityParameters
        securityParameters.l >> 32
        publicParameters.l_m >> 32
        publicParameters.l_r >> 16
        publicParameters.s >> 2
        publicParameters.a_x >> (defaultAlphabet as List<Character>)
        publicParameters.a_r >> (defaultAlphabet as List<Character>)
        publicParameters.k_x >> 3

        voteCastingClient = new VoteCastingClientAlgorithms(publicParameters, generalAlgorithms, randomGenerator, hash)
    }

    def "genBallot should generate a valid ballot (incl. OT query and used randomness)"() {
        given: "some known randomness"
        randomGenerator.randomInZq(_) >>> [
                ONE, // genQuery, r_1
                THREE, // genBallotProof, omega_1
                ONE // genBallotProof, omega_3
        ]
        randomGenerator.randomInGq(encryptionGroup) >> FIVE // genBallotProof, omega_2
        and: "some valid selected primes"
        generalAlgorithms.getPrimes(1) >> [THREE]
        and: "some arbitrary values for the proof challenge"
        // t_1 = g_circ ^ omega_1 mod p_circ = 3 ^ 3 mod 11 = 5
        // t_2 = omega_2 * pk ^ omega_3 mod p = 5 * 3 ^ 1 mod 11 = 4
        // t_3 = g ^ omega_3 mod p = 3 ^ 1 mod 11 = 3
        generalAlgorithms.getNIZKPChallenge(
                [ONE, NINE, THREE] as BigInteger[], // x_circ, a, b
                [FIVE, FOUR, THREE] as BigInteger[],  // t_1, t_2, t_3
                FIVE // min(q, q_circ)
        ) >> FOUR // c

        when: "generating a ballot"
        def ballotQueryAndRand = voteCastingClient.genBallot("f", [1], new EncryptionPublicKey(THREE, encryptionGroup))

        then: "x_circ has the expected value"
        // x = 5
        // x_circ = g_circ ^ x mod p_circ = 3 ^ 5 mod 11 = 1
        ballotQueryAndRand.alpha.x_circ == ONE

        and: "bold_a has the expected value"
        // m = 3
        // a_1 = q_1 * pk ^ r_1 mod p = 3 * 3 ^ 1 mod 11 = 9
        ballotQueryAndRand.alpha.bold_a == [NINE]

        and: "b has the expected value"
        // b = g ^ r_1 mod p = 3 ^ 1 mod 11 = 3
        ballotQueryAndRand.alpha.b == THREE

        and: "pi has the expected value"
        // for values of t_1 to t_3 see above
        // s_1 = omega_1 + c * x mod q_circ = 3 + 4 * 15 mod 5 = 3
        // s_2 = omega_2 * m ^ c mod p = 5 * 3 ^ 4 mod 11 = 9
        // s_3 = omega_3 + c * r mod q = 1 + 4 * 1 mod 5 = 0
        ballotQueryAndRand.alpha.pi == new NonInteractiveZKP(
                [FIVE, FOUR, THREE],
                [THREE, NINE, ZERO]
        )

        and: "the provided randomness is returned"
        ballotQueryAndRand.bold_r == [ONE]
    }

    def "getSelectedPrimes"() {
        given: "some valid selected primes"
        generalAlgorithms.getPrimes(1) >> [THREE]

        when:
        def selectedPrimes = voteCastingClient.getSelectedPrimes(Arrays.asList(1))

        then:
        selectedPrimes.size() == 1
        selectedPrimes.containsAll(THREE)
    }

    def "genQuery should generate a valid query for the ballot (incl. the randomness used)"() {
        given: "some known randomness"
        randomGenerator.randomInZq(_) >> ONE

        when: "generating a query"
        def query = voteCastingClient.genQuery([THREE], new EncryptionPublicKey(THREE, encryptionGroup))

        then:
        // a_1 = q_1 * pk ^ r_1 mod p = 3 * 3 ^ 1 mod 11 = 9
        query.bold_a == [NINE]
        query.bold_r == [ONE]
    }

    def "genBallotProof should generate a valid proof of knowledge of the ballot"() {
        given: "some known randomness"
        randomGenerator.randomInZq(_) >> THREE >> ONE // omega_1 and omega_3
        randomGenerator.randomInGq(encryptionGroup) >> FIVE // omega_2

        and: "some arbitrary values for the proof challenge"
        // t_1 = g_circ ^ omega_1 mod p_circ = 3 ^ 3 mod 11 = 5
        // t_2 = omega_2 * pk ^ omega_3 mod p = 5 * 3 ^ 1 mod 11 = 4
        // t_3 = g ^ omega_3 mod p = 3 ^ 1 mod 11 = 3
        generalAlgorithms.getNIZKPChallenge(
                [THREE, NINE, THREE] as BigInteger[], // x_circ, a, b
                [FIVE, FOUR, THREE] as BigInteger[],  // t_1, t_2, t_3
                FIVE // min(q, q_circ)
        ) >> FOUR // c

        when: "generating a ballot ZKP"
        def pi = voteCastingClient.genBallotProof(FIVE, THREE, ONE, THREE, NINE, THREE,
                new EncryptionPublicKey(THREE, encryptionGroup))

        then:
        // for values of t_1 to t_3 see above
        // s_1 = omega_1 + c * x mod q_circ = 3 + 4 * 5 mod 5 = 3
        // s_2 = omega_2 * m ^ c mod p = 5 * 3 ^ 4 mod p = 9
        // s_3 = omega_3 + c * r mod q = 1 + 4 * 1 mod 3 = 0
        pi == new NonInteractiveZKP(
                [FIVE, FOUR, THREE],
                [THREE, NINE, ZERO]
        )
    }

    def "getPointMatrix should compute the point matrix according to spec"() {
        given:
        ObliviousTransferResponse beta_1 = Mock()
        beta_1.b >> [ONE]
        beta_1.c >> [[0x01, 0x02, 0x03, 0x04], [0x05, 0x06, 0x07, 0x08], [0x0A, 0x0B, 0x0C, 0x0D]]
        beta_1.d >> [THREE]
        hash.recHash_L(ONE, ONE) >> ([0x0A, 0x0F, 0x0C, 0x0C] as byte[]) // b_i * d_j^{-r_i} mod p = 1 * 3^-5 mod 11 = 1

        ObliviousTransferResponse beta_2 = Mock()
        beta_2.b >> [FIVE]
        beta_2.c >> [[0x10, 0x20, 0x30, 0x40], [0x50, 0x60, 0x70, 0x80], [0xA0, 0xB0, 0xC0, 0xD0]]
        beta_2.d >> [FOUR]
        hash.recHash_L(FIVE, ONE) >> ([0xA0, 0xB3, 0xC0, 0xD0] as byte[]) // b_i * d_j^{-r_i} mod p = 5 * 4^-5 mod 11 = 5

        when:
        def pointMatrix = voteCastingClient.getPointMatrix([beta_1, beta_2], [1], [3], [FIVE])

        then:
        pointMatrix == [
                [new Point(FOUR, ONE)], // Authority 1
                [new Point(THREE, ZERO)] // Authority 2
        ]
    }

    def "getPoints should compute the points correctly from the authority's reply"() {
        given:
        ObliviousTransferResponse beta = Mock()
        beta.b >> [ONE]
        beta.c >> [[0x01, 0x02, 0x03, 0x04], [0x05, 0x06, 0x07, 0x08], [0x0A, 0x0B, 0x0C, 0x0D]]
        beta.d >> [THREE]
        hash.recHash_L(ONE, ONE) >> ([0x0A, 0x0F, 0x0C, 0x0C] as byte[]) // b_i * d_j^{-r_i} mod p = 1 * 3^-5 mod 11 = 1

        when:
        def points = voteCastingClient.getPoints(beta, [1], [3], [FIVE])

        then:
        points == [new Point(FOUR, ONE)]
    }

    def "getReturnCodes should combine the given point matrix into the return codes for the voter"() {
        given:
        def point11 = new Point(ONE, FOUR)
        def point21 = new Point(FIVE, THREE)
        def pointMatrix = [
                [ // authority 1
                  point11 // choice 1
                ],
                [ // authority 2
                  point21 // choice 1
                ]
        ]
        hash.recHash_L(point11) >> ([0x05, 0x06] as byte[])
        hash.recHash_L(point21) >> ([0xD1, 0xCF] as byte[])

        when:
        def rc = voteCastingClient.getReturnCodes(pointMatrix)

        then:
        rc.size() == 1
        rc[0] == "ntj" // [0xD4, 0xC9] -> 54473
    }
}
