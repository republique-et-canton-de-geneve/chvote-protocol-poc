package ch.ge.ve.protopoc.service.algorithm

import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.model.ObliviousTransferResponse
import ch.ge.ve.protopoc.service.model.PrimeField
import ch.ge.ve.protopoc.service.model.PublicParameters
import ch.ge.ve.protopoc.service.support.Hash
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE

/**
 * Tests for the Vote Casting algorithms
 */
class VoteCastingTest extends Specification {
    def Hash hash = Mock()
    def PublicParameters publicParameters = Mock();
    def EncryptionGroup encryptionGroup = Mock()
    def PrimeField primeField = Mock()

    def VoteCasting voteCasting

    void setup() {
        publicParameters.encryptionGroup >> encryptionGroup
        encryptionGroup.p >> SEVEN
        publicParameters.primeField >> primeField
        primeField.p_prime >> FIVE
        publicParameters.l_m >> 16
        publicParameters.l_r >> 16
        publicParameters.s >> 2

        voteCasting = new VoteCasting(publicParameters, hash)
    }

    def "getPoints should compute the points correctly from the authority's reply"() {
        given:
        def ObliviousTransferResponse beta = Mock()
        beta.b >> [ONE]
        beta.c >> [[0x01, 0x02, 0x03, 0x04], [0x05, 0x06, 0x07, 0x08], [0x0A, 0x0B, 0x0C, 0x0D]]
        beta.d >> [THREE]
        hash.hash(THREE) >> ([0x0A, 0x0F, 0x0C, 0x0C] as byte[]) // b_i * d_j^{-r_i} mod p = 1 * 3^-5 mod 7 = 3

        when:
        def points = voteCasting.getPoints(beta, [1], [2], [FIVE])

        then:
        points == [new Polynomial.Point(FOUR, ONE)]
    }

    def "getReturnCodes should combine the given point matrix into the return codes for the voter"() {
        given:
        def point11 = new Polynomial.Point(ONE, FOUR)
        def point21 = new Polynomial.Point(FIVE, THREE)
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
        def rc = voteCasting.getReturnCodes(pointMatrix)

        then:
        rc.length == 1
        rc[0] == ([0xD4, 0xC9] as byte[])
    }
}
