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

package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic;
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.model.IdentificationGroup;
import ch.ge.ve.protopoc.service.support.BigIntegers;
import ch.ge.ve.protopoc.service.support.ByteArrayUtils;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * This class regroups the general algorithms described in Section 7.2 of the specification
 */
public class GeneralAlgorithms {
    private final Hash hash;
    private final Conversion conversion;
    private final EncryptionGroup encryptionGroup;
    private final IdentificationGroup identificationGroup;
    private ImmutableList<BigInteger> cachedPrimes;

    /**
     * Constructor, defines all collaborators
     *
     * @param hash                the hash implementation
     * @param conversion          the conversion implementation
     * @param encryptionGroup     the encryption group used
     * @param identificationGroup the identification group used
     */
    public GeneralAlgorithms(Hash hash, Conversion conversion, EncryptionGroup encryptionGroup,
                             IdentificationGroup identificationGroup) {
        this.hash = hash;
        this.conversion = conversion;
        this.encryptionGroup = encryptionGroup;
        this.identificationGroup = identificationGroup;
    }

    /**
     * Algorithm 7.1: GetPrimes
     * <p>This implementation makes use of a cache, as suggested in the comment of the algorithm</p>
     *
     * @param n the number of requested primes
     * @return the ordered list of the n first primes found in the group
     */
    public List<BigInteger> getPrimes(int n) throws NotEnoughPrimesInGroupException {
        if (cachedPrimes.size() < n) {
            throw new IllegalStateException("The primes cache should have been populated beforehand");
        }
        return cachedPrimes.subList(0, n);
    }

    /**
     * Add a local primes cache, to save some time for primes computation
     *
     * @param n the requested size of the list
     * @throws NotEnoughPrimesInGroupException if the encryption group is too small to yield the requested number of $
     *                                         primes
     */
    public synchronized void populatePrimesCache(int n) throws NotEnoughPrimesInGroupException {
        Preconditions.checkState(cachedPrimes == null, "The primes cache can only be initialized" +
                "once...");
        BigInteger x = BigInteger.ONE;
        ImmutableList.Builder<BigInteger> cacheBuilder = ImmutableList.builder();
        int i = 0;
        while (i < n) {
            do {
                // Performance improvement over +1 / +2 defined in algorithm
                x = x.nextProbablePrime();
                if (x.compareTo(encryptionGroup.getP()) >= 0)
                    throw new NotEnoughPrimesInGroupException(
                            String.format("Only found %d primes (%s) in group %s",
                                    i,
                                    Joiner.on(",").join(
                                            cacheBuilder.build().stream().limit(4)
                                                    .collect(Collectors.toList())), encryptionGroup));
            } while (!x.isProbablePrime(100) || !isMember(x));
            cacheBuilder.add(x);
            i++;
        }

        cachedPrimes = cacheBuilder.build();
    }

    /**
     * Algorithm 7.2 : isMember
     *
     * @param x A number
     * @return true if x &isin; encryptionGroup, false otherwise
     */
    public boolean isMember(BigInteger x) {
        return x.compareTo(BigInteger.ONE) >= 0 && x.compareTo(encryptionGroup.getP()) < 0 &&
                BigIntegerArithmetic.jacobiSymbol(x, encryptionGroup.getP()) == 1;
    }

    /**
     * Utility to verify membership for G_q_hat
     *
     * @param x a number
     * @return true if x &isin; identificationGroup, false otherwise
     */
    public boolean isMember_G_q_hat(BigInteger x) {
        return x.compareTo(BigInteger.ONE) >= 0 && x.compareTo(identificationGroup.getP_hat()) < 0 &&
                BigIntegerArithmetic.jacobiSymbol(x, identificationGroup.getP_hat()) == 1;
    }

    /**
     * Utility to verify membership for Z_q
     *
     * @param x a number
     * @return true if x &isin; Z_q, false otherwise
     */
    public boolean isInZ_q(BigInteger x) {
        return x.compareTo(BigInteger.ZERO) >= 0 && x.compareTo(encryptionGroup.getQ()) < 0;
    }

    /**
     * Utility to verify membership for Z_q_hat
     *
     * @param x a number
     * @return true if x &isin; Z_q_hat, false otherwise
     */
    public boolean isInZ_q_hat(BigInteger x) {
        return x.compareTo(BigInteger.ZERO) >= 0 && x.compareTo(identificationGroup.getQ_hat()) < 0;
    }

    /**
     * Algorithm 7.3: GetGenerators
     * Create a number of independent generators for the encryption group given
     *
     * @param n number of generators to be computed
     * @return a list of independent generators
     */
    public List<BigInteger> getGenerators(int n) {
        List<BigInteger> h = new ArrayList<>();
        Set<BigInteger> valuesToAvoid = new HashSet<>();
        valuesToAvoid.add(BigInteger.ZERO);
        valuesToAvoid.add(BigInteger.ONE);
        valuesToAvoid.add(encryptionGroup.getG());
        valuesToAvoid.add(encryptionGroup.getH());

        for (int i = 0; i < n; i++) {
            BigInteger h_i;
            int x = 0;
            do {
                x++;
                byte[] bytes = hash.recHash_L("chVote", BigInteger.valueOf(i), BigInteger.valueOf(x));
                h_i = conversion.toInteger(bytes).mod(encryptionGroup.getP());
                h_i = h_i.multiply(h_i).mod(encryptionGroup.getP());
            } while (valuesToAvoid.contains(h_i)); // Very unlikely, but needs to be avoided
            h.add(h_i);
            valuesToAvoid.add(h_i);
        }
        return h;
    }

    /**
     * Algorithm 7.4: GetNIZKPChallenge
     *
     * @param y     the public values vector (domain unspecified)
     * @param t     the commitments vector (domain unspecified)
     * @param kappa the soundness strength of the challenge
     * @return the computed challenge
     */
    public BigInteger getNIZKPChallenge(Object[] y, Object[] t, int kappa) {
        return conversion.toInteger(hash.recHash_L(y, t)).mod(BigIntegers.TWO.pow(kappa));
    }

    /**
     * Algorithm 7.5: GetChallenges
     *
     * @param n     the number of challenges requested
     * @param y     the public values vector (domain unspecified)
     * @param kappa the soundness strength of the challenge
     * @return a list challenges, of length n
     */
    public List<BigInteger> getChallenges(int n, Object[] y, int kappa) {
        byte[] upper_h = hash.recHash_L(y);
        BigInteger two_to_kappa = BigIntegers.TWO.pow(kappa);
        Map<Integer, BigInteger> challengesMap = IntStream.rangeClosed(1, n).parallel().boxed()
                .collect(toMap(identity(), i -> {
                    byte[] upper_i = hash.recHash_L(BigInteger.valueOf(i));
                    return conversion.toInteger(hash.hash_L(ByteArrayUtils.concatenate(upper_h, upper_i)))
                            .mod(two_to_kappa);
                }));
        return IntStream.rangeClosed(1, n).mapToObj(challengesMap::get).collect(Collectors.toList());
    }
}
