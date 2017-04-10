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

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException
import ch.ge.ve.protopoc.service.model.EncryptionGroup
import ch.ge.ve.protopoc.service.model.IdentificationGroup
import ch.ge.ve.protopoc.service.support.Conversion
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.JacobiSymbol
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE

/**
 * This specification defines the expected behaviour of the general algorithms
 */
class GeneralAlgorithmsTest extends Specification {
    GeneralAlgorithms generalAlgorithms
    JacobiSymbol jacobiSymbol = Mock()
    Hash hash = Mock()
    Conversion conversion = new Conversion()

    static ELEVEN = BigInteger.valueOf(11L)

    EncryptionGroup eg = Mock()
    IdentificationGroup ig = Mock()

    void setup() {
        generalAlgorithms = new GeneralAlgorithms(hash, conversion, eg, ig)

        eg.p >> ELEVEN
        eg.q >> FIVE
        eg.g >> THREE
        eg.h >> FIVE
    }

    def "isMember"() {
        when:
        jacobiSymbol.computeJacobiSymbol(ONE, ELEVEN) >> 1

        then:
        generalAlgorithms.isMember(x) == result

        where:
        x      | result
        ONE    | true
        ELEVEN | false
    }

    def "getPrimes"() {
        given:
        jacobiSymbol.computeJacobiSymbol(THREE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(FIVE, ELEVEN) >> 1
        generalAlgorithms.populatePrimesCache(2)

        when:
        def primes = generalAlgorithms.getPrimes(2)

        then:
        primes.size() == 2
        primes.containsAll(THREE, FIVE)
    }

    def "populatePrimesCache not enough primes available"() {
        given:
        jacobiSymbol.computeJacobiSymbol(THREE, ELEVEN) >> 1
        jacobiSymbol.computeJacobiSymbol(FIVE, ELEVEN) >> 1

        when:
        generalAlgorithms.populatePrimesCache(3)

        then:
        thrown(NotEnoughPrimesInGroupException)
    }

    def "getGenerators"() {
        when:
        def generators = generalAlgorithms.getGenerators(2)

        then:
        4 * hash.recHash_L(_ as Object[]) >>> [
                [0x09] as byte[], // 9 * 9 = 81 =_11 4 --> OK
                [0x05] as byte[], // 5 * 5 = 25 =_11 3 --> KO, is g
                [0x01] as byte[], // 1 * 1 = 1 =_11 1 --> KO, is 1
                [0x03] as byte[] // 3 * 3 = 9 =_11 9 --> OK
        ]
        generators.containsAll(FOUR, NINE)
    }

    def "getNIZKPChallenge"() {
        Object[] v, t
        v = new Object[0]
        t = new Object[0]

        when:
        def challenge = generalAlgorithms.getNIZKPChallenge(v, t, 2)

        then:
        1 * hash.recHash_L(v, t) >> ([0x0F] as byte[])
        challenge == THREE
    }

    def "getChallenges"() {
        when:
        def challenges = generalAlgorithms.getChallenges(3, [] as Object[], 2)

        then:
        1 * hash.recHash_L([]) >> ([0x00] as byte[])
        1 * hash.recHash_L(ONE) >> ([0x01] as byte[])
        1 * hash.recHash_L(TWO) >> ([0x02] as byte[])
        1 * hash.recHash_L(THREE) >> ([0x03] as byte[])
        1 * hash.hash_L([0x00, 0x01] as byte[]) >> ([0x0A] as byte[])
        1 * hash.hash_L([0x00, 0x02] as byte[]) >> ([0x03] as byte[])
        1 * hash.hash_L([0x00, 0x03] as byte[]) >> ([0x1F] as byte[])

        challenges.containsAll(TWO, THREE, THREE)
    }
}
