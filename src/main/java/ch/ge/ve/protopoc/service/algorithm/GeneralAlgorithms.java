package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic;
import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class regroups the general algorithms described in Section 5.2 of the specification
 */
public class GeneralAlgorithms {
    private final List<BigInteger> cachedPrimes = new ArrayList<>();
    private final Hash hash;
    private final Conversion conversion;
    private final EncryptionGroup encryptionGroup;

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
     * Algorithm 5.1 : isMember
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
     * Algorithm 5.2: getPrimes
     *
     * @param n the number of requested primes
     * @return the ordered list of the n first primes found in the group
     */
    public List<BigInteger> getPrimes(int n) throws NotEnoughPrimesInGroupException {
        if (cachedPrimes.size() < n) {
            addPrimesToCache(n);
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
    private synchronized void addPrimesToCache(int n) throws NotEnoughPrimesInGroupException {
        BigInteger x;
        if (cachedPrimes.size() > 0) {
            x = cachedPrimes.get(cachedPrimes.size() - 1);
        } else {
            x = BigInteger.ONE;
        }
        while (cachedPrimes.size() < n) {
            do {
                // Performance improvement over +1 / +2 defined in algorithm
                x = x.nextProbablePrime();
                if (x.compareTo(encryptionGroup.getP()) >= 0)
                    throw new NotEnoughPrimesInGroupException(
                            String.format("Only found %d primes (%s) in group %s",
                                    cachedPrimes.size(),
                                    Joiner.on(",").join(
                                            cachedPrimes.stream().limit(4)
                                                    .collect(Collectors.toList())), encryptionGroup));
            } while (!x.isProbablePrime(100) || !isMember(x));
            cachedPrimes.add(x);
        }
    }


    /**
     * Algorithm 5.3: getSelectedPrimes
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
     * Algorithm 5.4: GetGenerators
     * Create a number of independent generators for the encryption group given
     *
     * @param n number of generators to be computed
     * @return a list of independent generators
     */
    public List<BigInteger> getGenerators(int n) {
        List<BigInteger> h = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            BigInteger h_i;
            int x = 0;
            do {
                x++;
                byte[] bytes = hash.hash("chVote", BigInteger.valueOf(i), BigInteger.valueOf(x));
                h_i = conversion.toInteger(bytes).mod(encryptionGroup.getP());
                h_i = h_i.multiply(h_i).mod(encryptionGroup.getP());
            } while (h_i.equals(BigInteger.ONE)); // Very unlikely, but needs to be avoided
            h.add(h_i);
        }
        return h;
    }

    /**
     * Algorithm 5.5: GetProofChallenge
     *
     * @param y    the public values vector (domain unspecified)
     * @param t    the commitments vector (domain unspecified)
     * @param c_ub the upper-bound of the challenge
     * @return the computed challenge
     */
    public BigInteger getNIZKPChallenge(Object[] y, Object[] t, BigInteger c_ub) {
        return conversion.toInteger(hash.hash(y, t)).mod(c_ub);
    }

    /**
     * Algorithm 5.6: GetPublicChallenges
     *
     * @param n    the number of challenges requested
     * @param v    the public values vector (domain unspecified)
     * @param c_ub the upper-bound of the challenge
     * @return a list challenges, of length n
     */
    public List<BigInteger> getChallenges(int n, Object[] v, BigInteger c_ub) {
        List<BigInteger> c = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            BigInteger c_i = conversion.toInteger(hash.hash(v, BigInteger.valueOf(i))).mod(c_ub);
            c.add(c_i);
        }
        return c;
    }
}
