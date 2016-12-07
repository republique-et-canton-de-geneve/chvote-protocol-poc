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
    PrimeField primeField = Mock()
    RandomGenerator randomGenerator = Mock()
    GeneralAlgorithms generalAlgorithms = Mock()

    VoteCastingClientAlgorithms voteCastingClient

    void setup() {
        def defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray()

        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> SEVEN
        encryptionGroup.q >> THREE
        encryptionGroup.g >> TWO
        publicParameters.identificationGroup >> identificationGroup
        identificationGroup.p_circ >> ELEVEN
        identificationGroup.q_circ >> FIVE
        identificationGroup.g_circ >> THREE
        publicParameters.primeField >> primeField
        primeField.p_prime >> FIVE
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
                ZERO, // genQuery, r_2
                THREE, // genBallotProof, omega_1
                ONE // genBallotProof, omega_3
        ]
        randomGenerator.randomInGq(encryptionGroup) >> TWO // genBallotProof, omega_2
        and: "some valid selected primes"
        generalAlgorithms.getSelectedPrimes([1, 2]) >> [TWO, THREE]
        and: "some arbitrary values for the proof challenge"
        // t_1 = g_circ ^ omega_1 mod p_circ = 3^3 mod 11 = 5
        // t_2 = omega_2 * pk ^ omega_3 mod p = 2 * 3 ^ 1 mod 7 = 6
        // t_3 = g ^ omega_3 mod p = 2 ^ 1 mod 7 = 2
        generalAlgorithms.getProofChallenge(
                [ONE, FOUR, TWO] as BigInteger[], // x_circ, a, b
                [FIVE, SIX, TWO] as BigInteger[],  // t_1, t_2, t_3
                THREE // min(q, q_circ)
        ) >> FIVE // c

        when: "generating a ballot"
        def ballotQueryAndRand = voteCastingClient.genBallot("p", [1, 2], new EncryptionPublicKey(THREE, encryptionGroup))

        then: "x_circ has the expected value"
        // x = 15
        // x_circ = g_circ ^ x mod p_circ = 3 ^ 15 mod 11 = 1
        ballotQueryAndRand.alpha.x_circ == ONE

        and: "bold_a has the expected value"
        // u = 2 * 3 = 6
        // a_1 = u_1 * pk ^ r_1 mod p = 2 * 3 ^ 1 mod 7 = 6
        // a_2 = u_2 * pk ^ r_2 mod p = 3 * 3 ^ 0 mod 7 = 3
        ballotQueryAndRand.alpha.bold_a == [SIX, THREE]

        and: "b has the expected value"
        // b = g ^ (r_1 + r_2) mod p = 2 ^ 1 mod 7 = 2
        ballotQueryAndRand.alpha.b == TWO

        and: "pi has the expected value"
        // for values of t_1 to t_3 see above
        // s_1 = omega_1 + c * x mod q_circ = 3 + 5 * 15 mod 5 = 3
        // s_2 = omega_2 * u ^ c mod p = 2 * 6 ^ 5 mod p = 5
        // s_3 = omega_3 + c * r mod q = 1 + 5 * 1 mod 3 = 0
        ballotQueryAndRand.alpha.pi == new NonInteractiveZKP(
                [FIVE, SIX, TWO],
                [THREE, FIVE, ZERO]
        )

        and: "the provided randomness is returned"
        ballotQueryAndRand.bold_r == [ONE, ZERO]
    }

    def "genQuery should generate a valid query for the ballot (incl. the randomness used)"() {
        given: "some known randomness"
        randomGenerator.randomInZq(_) >> ONE >> ZERO

        when: "generating a query"
        def query = voteCastingClient.genQuery([TWO, THREE], new EncryptionPublicKey(THREE, encryptionGroup))

        then:
        // a_1 = u_1 * pk ^ r_1 mod p = 2 * 3 ^ 1 mod 7 = 6
        // a_2 = u_2 * pk ^ r_2 mod p = 3 * 3 ^ 0 mod 7 = 3
        query.bold_a == [SIX, THREE]
        query.bold_r == [ONE, ZERO]
    }

    def "genBallotProof should generate a valid proof of knowledge of the ballot"() {
        given: "some known randomness"
        randomGenerator.randomInZq(_) >> TWO >> ONE // omega_1 and omega_3
        randomGenerator.randomInGq(encryptionGroup) >> TWO // omega_2

        and: "some arbitrary values for the proof challenge"
        // t_1 = g_circ ^ omega_1 mod p_circ = 3^2 mod 11 = 9
        // t_2 = omega_2 * pk ^ omega_3 mod p = 2 * 3 ^ 1 mod 7 = 6
        // t_3 = g ^ omega_3 mod p = 2 ^ 1 mod 7 = 2
        generalAlgorithms.getProofChallenge(
                [THREE, FOUR, FOUR] as BigInteger[], // x_circ, a, b
                [NINE, SIX, TWO] as BigInteger[],  // t_1, t_2, t_3
                THREE // min(q, q_circ)
        ) >> FIVE // c

        when: "generating a ballot ZKP"
        def pi = voteCastingClient.genBallotProof(ONE, SIX, TWO, THREE, FOUR, FOUR, new EncryptionPublicKey(THREE, encryptionGroup))

        then:
        // for values of t_1 to t_3 see above
        // s_1 = omega_1 + c * x mod q_circ = 2 + 5 * 1 mod 5 = 2
        // s_2 = omega_2 * u ^ c mod p = 2 * 6 ^ 5 mod p = 5
        // s_3 = omega_3 + c * r mod q = 1 + 5 * 2 mod 3 = 2
        pi == new NonInteractiveZKP(
                [NINE, SIX, TWO],
                [TWO, FIVE, TWO]
        )
    }

    def "getPointMatrix should compute the point matrix according to spec"() {
        given:
        ObliviousTransferResponse beta_1 = Mock()
        beta_1.b >> [ONE]
        beta_1.c >> [[0x01, 0x02, 0x03, 0x04], [0x05, 0x06, 0x07, 0x08], [0x0A, 0x0B, 0x0C, 0x0D]]
        beta_1.d >> [THREE]
        hash.hash(THREE) >> ([0x0A, 0x0F, 0x0C, 0x0C] as byte[]) // b_i * d_j^{-r_i} mod p = 1 * 3^-5 mod 7 = 3

        ObliviousTransferResponse beta_2 = Mock()
        beta_2.b >> [TWO]
        beta_2.c >> [[0x10, 0x20, 0x30, 0x40], [0x50, 0x60, 0x70, 0x80], [0xA0, 0xB0, 0xC0, 0xD0]]
        beta_2.d >> [FOUR]
        hash.hash(ONE) >> ([0xA0, 0xB3, 0xC0, 0xD0] as byte[]) // b_i * d_j^{-r_i} mod p = 2 * 4^-5 mod 7 = 1

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
        hash.hash(THREE) >> ([0x0A, 0x0F, 0x0C, 0x0C] as byte[]) // b_i * d_j^{-r_i} mod p = 1 * 3^-5 mod 7 = 3

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
        hash.hash(point11) >> ([0x05, 0x06] as byte[])
        hash.hash(point21) >> ([0xD1, 0xCF] as byte[])

        when:
        def rc = voteCastingClient.getReturnCodes(pointMatrix)

        then:
        rc.size() == 1
        rc[0] == "ntj" // [0xD4, 0xC9] -> 54473
    }
}
