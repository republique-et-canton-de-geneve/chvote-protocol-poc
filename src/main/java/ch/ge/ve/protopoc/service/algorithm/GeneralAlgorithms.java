package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.support.JacobiSymbol;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class regroups the general algorithms described in Section 5.2 of the specification
 */
public class GeneralAlgorithms {
    private final JacobiSymbol jacobiSymbol;

    /**
     * Constructor, defines all collaborators
     *
     * @param jacobiSymbol the jacobiSymbol computing class
     */
    public GeneralAlgorithms(JacobiSymbol jacobiSymbol) {
        this.jacobiSymbol = jacobiSymbol;
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
}
