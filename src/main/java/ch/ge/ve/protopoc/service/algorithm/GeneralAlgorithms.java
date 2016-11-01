package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.JacobiSymbol;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class regroups the general algorithms described in Section 5.2 of the specification
 */
public class GeneralAlgorithms {
    private final JacobiSymbol jacobiSymbol;
    private final Hash hash;
    private final Conversion conversion;

    /**
     * Constructor, defines all collaborators
     *  @param jacobiSymbol the jacobiSymbol computing class
     * @param hash
     * @param conversion
     */
    public GeneralAlgorithms(JacobiSymbol jacobiSymbol, Hash hash, Conversion conversion) {
        this.jacobiSymbol = jacobiSymbol;
        this.hash = hash;
        this.conversion = conversion;
    }

    /**
     * Algorithm 5.1 : isMember
     *
     * @param x  A number
     * @param eg An encryption group
     * @return true if x &isin; eg, false otherwise
     */
    public boolean isMember(BigInteger x, EncryptionGroup eg) {
        if (x.compareTo(BigInteger.ONE) >= 0 &&
                x.compareTo(eg.p) <= -1) {
            return jacobiSymbol.computeJacobiSymbol(x, eg.p) == 1;
        } else {
            return false;
        }
    }

    /**
     * Algorithm 5.2: getPrimes
     *
     * @param n  the number of requested primes
     * @param eg the encryption group for which we want to generate primes
     * @return the ordered list of the n first primes found in the group
     */
    public List<BigInteger> getPrimes(int n, EncryptionGroup eg) throws NotEnoughPrimesInGroupException {
        BigInteger x = BigInteger.ONE;
        List<BigInteger> primes = new ArrayList<>();
        while (primes.size() < n) {
            do {
                // Performance improvement over +1 / +2 defined in algorithm
                x = x.nextProbablePrime();
                if (x.compareTo(eg.p) >= 0)
                    throw new NotEnoughPrimesInGroupException(String.format("Only found %d primes (%s) in group %s", primes.size(), Joiner.on(",").join(primes.stream().limit(4).collect(Collectors.toList())), eg));
            } while (!x.isProbablePrime(100) || !isMember(x, eg));
            primes.add(x);
        }
        return primes;
    }

    /**
     * Algorithm 5.3: getSelectedPrimes
     *
     * @param selections the indices of the selected primes (in increasing order, 1-based)
     * @param eg         the encryption group
     * @return the list of the primes selected
     */
    public List<BigInteger> getSelectedPrimes(List<Integer> selections, EncryptionGroup eg) throws NotEnoughPrimesInGroupException {
        Preconditions.checkArgument(selections.stream().allMatch(i -> i >= 1));
        Preconditions.checkArgument(
                selections.equals(selections.stream().sorted().collect(Collectors.toList())),
                "The elements are not sorted!");
        Integer s_k = selections.get(selections.size() - 1);
        List<BigInteger> primes = getPrimes(s_k, eg);
        List<BigInteger> selectedPrimes = new ArrayList<>();
        for (Integer s_i : selections) {
            selectedPrimes.add(primes.get(s_i - 1));
        }

        return selectedPrimes;
    }

    /**
     * Algorithm 5.4: GetGenerators
     * Create a number of independent generators for the encryption group given
     *
     * @param n number of generators to be computed
     * @param eg the encryption group for which we want to create generators
     * @return a list of independent generators
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public List<BigInteger> getGenerators(int n, EncryptionGroup eg) throws NoSuchProviderException, NoSuchAlgorithmException {
        List<BigInteger> h = new ArrayList<>();
        int i = 1;
        while (h.size() < n) {
            BigInteger h_i;
            int x = 0;
            do {
                x++;
                byte[] bytes = hash.hash("chVote", BigInteger.valueOf(i), BigInteger.valueOf(x));
                h_i = conversion.toInteger(bytes).mod(eg.p);
            } while (h_i.equals(BigInteger.ONE)); // Very unlikely, but needs to be avoided
            h.add(h_i);
        }
        return h;
    }
}
