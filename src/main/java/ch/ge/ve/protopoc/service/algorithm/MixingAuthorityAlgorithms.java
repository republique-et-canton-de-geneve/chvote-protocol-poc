package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

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
     * Algorithm 5.41: GenShuffle
     *
     * @param bold_e the list of ElGamal encryptions
     * @param pk     the encryption key
     * @return the result of a shuffle, with re-encryption of the values
     */
    public Shuffle genShuffle(List<Encryption> bold_e, EncryptionPublicKey pk) {
        List<Integer> psy = genPermutation(bold_e.size());
        List<ReEncryption> reEncryptions = bold_e.stream()
                .map(e_i -> genReEncryption(e_i, pk)).collect(Collectors.toList());

        List<Encryption> bold_e_prime = psy.stream()
                .map(reEncryptions::get)
                .map(ReEncryption::getEncryption)
                .collect(Collectors.toList());

        List<BigInteger> bold_r_prime = reEncryptions.stream()
                .map(ReEncryption::getRandomness)
                .collect(Collectors.toList());

        return new Shuffle(bold_e_prime, bold_r_prime, psy);
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

    /**
     * Algorithm 5.46: GenPermutationCommitment
     *
     * @param psy    the permutation
     * @param bold_h a list of independent generators
     * @return a commitment to the permutation
     */
    public PermutationCommitment genPermutationCommitment(List<Integer> psy, List<BigInteger> bold_h) {
        Preconditions.checkArgument(psy.size() == bold_h.size(),
                "The lengths of psy and bold_h should be identical");
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();

        List<BigInteger> bold_c = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        // Loop indexed over j_i instead of i, for performance reasons, with a reverse permutation lookup
        List<Integer> reversePsy = reversePermutation(psy);
        for (int j_i = 0; j_i < psy.size(); j_i++) {
            Integer i = reversePsy.get(j_i);
            Preconditions.checkState(psy.get(i) == j_i, "The reverse permutation is not valid");

            BigInteger r_j_i = randomGenerator.randomInZq(q);
            BigInteger c_j_i = g.modPow(r_j_i, p).multiply(bold_h.get(i)).mod(p);
            bold_c.add(c_j_i);
            bold_r.add(r_j_i);
        }

        return new PermutationCommitment(bold_c, bold_r);
    }

    /**
     * Algorithm 5.47: GenCommitmentChain
     *
     * @param bold_u_prime the permuted challenges
     * @return a commitment chain relative to the permuted list of public challenges
     */
    public CommitmentChain genCommitmentChain(List<BigInteger> bold_u_prime) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        BigInteger h = publicParameters.getEncryptionGroup().getH();

        List<BigInteger> bold_c = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        bold_c.add(h); // c_0, we'll remove it afterwards

        for (int i = 0; i < bold_u_prime.size(); i++) {
            BigInteger c_i_minus_one = bold_c.get(i); // offset by one, due to adding c_0 as a prefix
            BigInteger u_prime_i = bold_u_prime.get(i);

            BigInteger r_i = randomGenerator.randomInZq(q);
            BigInteger c_i = g.modPow(r_i, p).multiply(c_i_minus_one.modPow(u_prime_i, p)).mod(p);

            bold_c.add(c_i);
            bold_r.add(r_i);
        }

        bold_c.remove(0);

        return new CommitmentChain(bold_c, bold_r);
    }

    private List<Integer> reversePermutation(List<Integer> psy) {
        Preconditions.checkArgument(psy.containsAll(IntStream.range(0, psy.size())
                        .mapToObj(Integer::valueOf).collect(Collectors.toList())),
                "The permutation should contain all number from 0 (inclusive) to length (exclusive)");

        return IntStream.range(0, psy.size()).mapToObj(psy::indexOf).collect(Collectors.toList());
    }
}
