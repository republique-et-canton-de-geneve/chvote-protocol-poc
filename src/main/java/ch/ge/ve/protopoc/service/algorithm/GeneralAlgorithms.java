package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic;
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.EncryptionGroup;
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
    private ImmutableList<BigInteger> cachedPrimes;

    /**
     * Constructor, defines all collaborators
     *
     * @param hash
     * @param conversion
     * @param encryptionGroup
     */
    public GeneralAlgorithms(Hash hash, Conversion conversion, EncryptionGroup encryptionGroup) {
        this.hash = hash;
        this.conversion = conversion;
        this.encryptionGroup = encryptionGroup;
    }

    /**
     * Algorithm 7.1 : isMember
     *
     * @param x A number
     * @return true if x &isin; encryptionGroup, false otherwise
     */
    public boolean isMember(BigInteger x) {
        if (x.compareTo(BigInteger.ONE) >= 0 &&
                x.compareTo(encryptionGroup.getP()) <= -1) {
            return BigIntegerArithmetic.jacobiSymbol(x, encryptionGroup.getP()) == 1;
        } else {
            return false;
        }
    }

    /**
     * Algorithm 7.2: getPrimes
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
     * Algorithm 7.3: getSelectedPrimes
     *
     * @param selections the indices of the selected primes (in increasing order, 1-based)
     * @return the list of the primes selected
     */
    public List<BigInteger> getSelectedPrimes(List<Integer> selections) throws NotEnoughPrimesInGroupException {
        Preconditions.checkArgument(selections.stream().allMatch(i -> i >= 1));
        Preconditions.checkArgument(
                selections.equals(selections.stream().sorted().collect(Collectors.toList())),
                "The elements are not sorted!");
        Integer s_k = selections.get(selections.size() - 1);
        List<BigInteger> primes = getPrimes(s_k);

        return selections.stream()
                .map(s_i -> primes.get(s_i - 1))
                .collect(Collectors.toList());
    }

    /**
     * Algorithm 7.4: GetGenerators
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
     * Algorithm 7.5: GetNIZKPChallenge
     *
     * @param y    the public values vector (domain unspecified)
     * @param t    the commitments vector (domain unspecified)
     * @param c_ub the upper-bound of the challenge
     * @return the computed challenge
     */
    public BigInteger getNIZKPChallenge(Object[] y, Object[] t, BigInteger c_ub) {
        return conversion.toInteger(hash.recHash_L(y, t)).mod(c_ub);
    }

    /**
     * Algorithm 7.6: GetChallenges
     *
     * @param n    the number of challenges requested
     * @param y    the public values vector (domain unspecified)
     * @param c_ub the upper-bound of the challenge
     * @return a list challenges, of length n
     */
    public List<BigInteger> getChallenges(int n, Object[] y, BigInteger c_ub) {
        byte[] upper_h = hash.recHash_L(y);
        Map<Integer, BigInteger> challengesMap = IntStream.range(1, n + 1).parallel().mapToObj(Integer::valueOf)
                .collect(toMap(identity(), i -> {
                    byte[] upper_i = hash.recHash_L(BigInteger.valueOf(i));
                    return conversion.toInteger(hash.hash_L(ByteArrayUtils.concatenate(upper_h, upper_i))).mod(c_ub);
                }));
        return IntStream.range(1, n + 1).mapToObj(challengesMap::get).collect(Collectors.toList());
    }
}
