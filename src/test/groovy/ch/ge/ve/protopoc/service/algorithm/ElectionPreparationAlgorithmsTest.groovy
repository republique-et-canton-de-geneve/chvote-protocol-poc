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

import ch.ge.ve.protopoc.service.model.*
import ch.ge.ve.protopoc.service.model.polynomial.Point
import ch.ge.ve.protopoc.service.support.Hash
import ch.ge.ve.protopoc.service.support.RandomGenerator
import spock.lang.Specification

import static ch.ge.ve.protopoc.service.support.BigIntegers.*
import static java.math.BigInteger.ONE
import static java.math.BigInteger.ZERO

/**
 * Tests on the algorithms performed during election preparation
 */
class ElectionPreparationAlgorithmsTest extends Specification {
    // Mocks
    Hash hash = Mock()
    RandomGenerator randomGenerator = Mock()
    IdentificationGroup identificationGroup = new IdentificationGroup(SEVEN, THREE, THREE)
    PrimeField primeField = new PrimeField(SEVEN)
    PublicParameters publicParameters = new PublicParameters(
            new SecurityParameters(1, 1, 1, 0.9),
            new EncryptionGroup(ELEVEN, FIVE, THREE, FOUR),
            identificationGroup,
            primeField,
            THREE,
            ['a', 'b'] as List<Character>,
            THREE,
            ['a', 'b'] as List<Character>,
            ['a', 'b'] as List<Character>,
            1,
            ['a', 'b'] as List<Character>,
            1,
            4,
            4
    )

    ElectionPreparationAlgorithms electionPreparation

    void setup() {
        electionPreparation = new ElectionPreparationAlgorithms(publicParameters, randomGenerator, hash)
    }

    def "genElectorateData should generate the expected electorate data"() {
        given: "two elections and two voters"
        DomainOfInfluence doi1 = new DomainOfInfluence("test 1")
        DomainOfInfluence doi2 = new DomainOfInfluence("test 2")
        Voter voter1 = new Voter()
        Voter voter2 = new Voter()
        and: "the first election is a 1-out-of-3 election (typical referendum)"
        Election election1 = new Election(3, 1, doi1)
        and: "the second election is a 2-out-of-4 election"
        Election election2 = new Election(4, 2, doi2)

        and: "both voters are eligible for the first election"
        def voters = [voter1, voter2]
        voters.forEach { it.addDomainsOfInfluence(doi1) }


        and: "only the first voter is eligible for the second election"
        voter1.addDomainsOfInfluence(doi2)


        and: "the matching number of candidates"
        Candidate candidate1 = new Candidate("e1 c1")
        Candidate candidate2 = new Candidate("e1 c2")
        Candidate candidate3 = new Candidate("e1 c3")
        Candidate candidate4 = new Candidate("e2 c1")
        Candidate candidate5 = new Candidate("e2 c2")
        Candidate candidate6 = new Candidate("e2 c3")
        Candidate candidate7 = new Candidate("e2 c4")
        def candidates = [
                candidate1,
                candidate2,
                candidate3,
                candidate4,
                candidate5,
                candidate6,
                candidate7
        ]
        def elections = [election1, election2]

        and: "the corresponding election set"
        ElectionSet electionSet = new ElectionSet(voters, candidates, elections)

        and: "the following pre-established 'random' values"
        randomGenerator.randomInZq(_) >>> [
                TWO, // GenPolynomial for voter 1, election 1, first and only coeff (k_1_1 = 1)
                SIX, //  GenPoint for voter 1, election 1, first candidate
                ONE, //  GenPoint for voter 1, election 1, second candidate
                TWO, //  GenPoint for voter 1, election 1, third candidate
                ONE, //  GenPolynomial for voter 1, election 2, first coeff (k_1_2 = 2)
                THREE, //  GenPolynomial for voter 1, election 2, second coeff (k_1_2 = 2)
                TWO, //  GenPoint for voter 1, election 2, first candidate
                FIVE, //  GenPoint for voter 1, election 2, second candidate
                ONE, //  GenPoint for voter 1, election 2, third candidate
                FOUR, //  GenPoint for voter 1, election 2, fourth candidate
                THREE, // GenSecretVoterData for voter 1, x
                TWO, // GenSecretVoterData for voter 1, y
                THREE, // GenPolynomial for voter 2, election 1, first and only coeff (k_1_1 = 1)
                FIVE, //  GenPoint for voter 2, election 1, first candidate
                FOUR, //  GenPoint for voter 2, election 1, second candidate
                THREE, //  GenPoint for voter 2, election 1, third candidate
                TWO, //  GenPoint for voter 2, election 2, first candidate
                ONE, //  GenPoint for voter 2, election 2, second candidate
                SIX, //  GenPoint for voter 2, election 2, third candidate
                FIVE, //  GenPoint for voter 2, election 2, fourth candidate
                ONE, // GenSecretVoterData for voter 2, x
                ONE // GenSecretVoterData for voter 2, y
        ]

        and: "the following computed hashes"
        hash.recHash_L(_) >> ([0x0C] as byte[])

        when: "the electorate data is generated"
        def electorateData = electionPreparation.genElectorateData(electionSet)

        then: "the result should have one set of secret voter data per voter"
        electorateData.d == [
                new SecretVoterData(
                        THREE,
                        TWO,
                        [0x0C] as byte[],
                        [[0x0C], [0x0C], [0x0C], [0x0C], [0x0C], [0x0C], [0x0C]] as byte[][]), // voter 1
                new SecretVoterData(
                        ONE,
                        ONE,
                        [0x0C] as byte[],
                        [[0x0C], [0x0C], [0x0C], [0x0C], [0x0C], [0x0C], [0x0C]] as byte[][]) // voter 2
        ]

        and: "one set of public voter data per voter"
        electorateData.d_hat == [
                new Point(SIX, TWO), // voter 1 -- x = 3^3 % 7 = 6; y = (2 + 0x0C) % 3 = 2; y_hat = 3^2 % 7 = 2
                new Point(THREE, THREE) // voter 2 -- x = 3^1 % 7 = 3; y = (1 + 0x0C) % 3 = 1; y_hat = 3^1 % 7 = 3
        ]

        and: "one set of points per voter"
        electorateData.p == [
                [ // voter 1
                  new Point(SIX, THREE), // election 1, candidate 1
                  new Point(ONE, THREE), // election 1, candidate 2
                  new Point(TWO, THREE), // election 1, candidate 3
                  new Point(TWO, TWO), // election 2, candidate 1 (y = 1 + 4 * 2 mod 7 = 2)
                  new Point(FIVE, ZERO), // election 2, candidate 2 (y = 1 + 4 * 5 mod 7 = 0)
                  new Point(ONE, FIVE), // election 2, candidate 3 (y = 1 + 4 * 1 mod 7 = 5)
                  new Point(FOUR, THREE) // election 2, candidate 4 (y = 1 + 4 * 4 mod 7 = 3)
                ],
                [ // voter 2
                  new Point(FIVE, FOUR), // election 1, candidate 1
                  new Point(FOUR, FOUR), // election 1, candidate 2
                  new Point(THREE, FOUR), // election 1, candidate 3
                  new Point(TWO, ZERO), // election 2, candidate 1
                  new Point(ONE, ZERO), // election 2, candidate 2
                  new Point(SIX, ZERO), // election 2, candidate 3
                  new Point(FIVE, ZERO) // election 2, candidate 4
                ]
        ]

        and: "the allowed selections matrix should take the eligibility matrix into account"
        electorateData.k == [[1, 2], [1, 0]]
    }

    def "genSecretVoterData should generate the expected private voter data"() {
        given:
        Point point1 = new Point(ONE, ZERO)
        Point point2 = new Point(ZERO, ONE)
        hash.recHash_L([point1, point2] as Object[]) >> ([0x03] as byte[])
        hash.recHash_L(point1) >> ([0x05] as byte[])
        hash.recHash_L(point2) >> ([0x07] as byte[])
        randomGenerator.randomInZq(_) >>> [FIVE, THREE]

        when:
        def secretData = electionPreparation.genSecretVoterData([point1, point2])

        then:
        secretData.x == FIVE
        secretData.y == THREE
        secretData.f == ([0x03] as byte[])
        secretData.rc == ([[0x05], [0x07]] as byte[][])
    }

    def "genPublicVoterData should generate the expected public voter data"() {
        given:
        hash.recHash_L(yList.toArray()) >> hashed

        expect:
        electionPreparation.getPublicVoterData(x, y, yList) == point

        where:
        x     | y   | yList        | hashed                 || point
        THREE | TWO | [FIVE, FOUR] | [0x13] as byte[]       || new Point(SIX, ONE) // x_hat = 3^3 % 7 = 6; y = (2 + 19) % 3 = 0 --> y_hat = 3^0 mod 7 = 1
        SIX   | ONE | [TWO, THREE] | [0x10, 0x30] as byte[] || new Point(ONE, TWO) // x_hat = 3^6 % 7 = 1; y = 1 + 0x1030 % 3 = 2 --> y_hat = 2^2 mod 7 = 2
    }

    def "getPublicCredentials should combine the public data from the different authorities"() {
        given:
        def upper_d_hat = [
                [ // authority 1
                  new Point(THREE, FOUR),
                  new Point(ONE, TWO)
                ], [ // authority 2
                     new Point(FIVE, ONE),
                     new Point(ONE, TWO)
                ], [ // authority 3
                     new Point(THREE, FIVE),
                     new Point(ONE, TWO)
                ], [ // authority 4
                     new Point(FOUR, TWO),
                     new Point(ONE, TWO)
                ]]

        when:
        def credentials = electionPreparation.getPublicCredentials(upper_d_hat)

        then:
        credentials == [
                new Point(FIVE, FIVE),
                new Point(ONE, TWO)
        ]
    }
}
