package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.support.RandomGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Algorithms performed during the mixing phase, by the autorities
 */
public class MixingAuthorityAlgorithms {
    private final PublicParameters publicParameters;
    private final VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms;
    private final RandomGenerator randomGenerator;

    public MixingAuthorityAlgorithms(PublicParameters publicParameters, VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms, RandomGenerator randomGenerator) {
        this.publicParameters = publicParameters;
        this.voteConfirmationAuthorityAlgorithms = voteConfirmationAuthorityAlgorithms;
        this.randomGenerator = randomGenerator;
    }

    /**
     * Algorithm 5.40: GetEncryptions
     *
     * @param ballotList       the list of ballots submitted to the bulletin board
     * @param confirmationList the list of confirmations submitted to the bulletin board
     * @return the list of the encryptions for the valid, confirmed ballots
     */
    public List<Encryption> getEncryptions(List<BallotEntry> ballotList, List<ConfirmationEntry> confirmationList) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();

        return ballotList.stream()
                .filter(B -> voteConfirmationAuthorityAlgorithms.hasConfirmation(B.getI(), confirmationList))
                .map(B -> {
                    BigInteger a_j = B.getAlpha().getBold_a().stream()
                            .reduce(BigInteger::multiply)
                            .orElseThrow(() -> new IllegalArgumentException("can't happen if protocol was followed"))
                            .mod(p);
                    return new Encryption(a_j, B.getAlpha().getB());
                })
                .collect(Collectors.toList());
    }

    /**
     * Algorithm 5.42: GenPermutation
     *
     * @param n the permutation size
     * @return a random permutation following Knuth's shuffle algorithm (permutation is 0 based, to mirror java indices)
     */
    public List<Integer> genPermutation(int n) {
        Integer[] I = IntStream.range(0, n)
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toList()).toArray(new Integer[0]);

        List<Integer> psy = new ArrayList<>();

        // indices are 0 base, as opposed to the 1 based in the algorithm
        for (int i = 0; i < n; i++) {
            int k = randomGenerator.randomIntInRange(i, n - 1);
            psy.add(I[k]);
            I[k] = I[i];
        }

        return psy;
    }

    /**
     * Algorithm 5.43: GenReEncryption
     *
     * @param e         the original encryption
     * @param publicKey the public key used
     * @return
     */
    public ReEncryption genReEncryption(Encryption e, EncryptionPublicKey publicKey) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        BigInteger pk = publicKey.getPublicKey();

        BigInteger r_prime = randomGenerator.randomInZq(q);

        BigInteger a_prime = e.getA().multiply(pk.modPow(r_prime, p)).mod(p);
        BigInteger b_prime = e.getB().multiply(g.modPow(r_prime, p)).mod(p);

        return new ReEncryption(new Encryption(a_prime, b_prime), r_prime);
    }
}
