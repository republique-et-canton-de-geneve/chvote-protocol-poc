package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Algorithms performed during the mixing phase, by the autorities
 */
public class MixingAuthorityAlgorithms {
    private final PublicParameters publicParameters;
    private final GeneralAlgorithms generalAlgorithms;
    private final VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms;
    private final RandomGenerator randomGenerator;

    public MixingAuthorityAlgorithms(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms, VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms, RandomGenerator randomGenerator) {
        this.publicParameters = publicParameters;
        this.generalAlgorithms = generalAlgorithms;
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
     * @return a re-encryption of the provided ElGamal encryption
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
     * Algorithm 5.44: GenShuffleProof
     *
     * @param bold_e       the vector of ElGamal encryptions
     * @param bold_e_prime the vector of permuted ElGamal re-encryptions
     * @param bold_r_prime the randomizations used for the re-encryption
     * @param psy          the permutation used
     * @param publicKey    the public key for the encryption
     * @return a proof of the validity of the shuffle, as per Wikström's
     * <em><strong>A commitment-consistent proof of a shuffle</strong></em>
     */
    public ShuffleProof genShuffleProof(List<Encryption> bold_e, List<Encryption> bold_e_prime,
                                        List<BigInteger> bold_r_prime, List<Integer> psy,
                                        EncryptionPublicKey publicKey) {
        int N = bold_e.size();
        Preconditions.checkArgument(bold_e_prime.size() == N,
                "The length of bold_e_prime should be equal to that of bold_e");
        Preconditions.checkArgument(bold_r_prime.size() == N,
                "The length of bold_r_prime should be equal to that of bold_e");
        Preconditions.checkArgument(psy.size() == N,
                "The length of psy should be equal to that of bold_e");

        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        BigInteger h = publicParameters.getEncryptionGroup().getH();

        BigInteger pk = publicKey.getPublicKey();


        List<BigInteger> bold_h = generalAlgorithms.getGenerators(N);
        PermutationCommitment permutationCommitment = genPermutationCommitment(psy, bold_h);
        List<BigInteger> bold_c = permutationCommitment.getBold_c();
        List<BigInteger> bold_r = permutationCommitment.getBold_r();
        List<BigInteger> bold_u = generalAlgorithms.getChallenges(N, new List[]{bold_e, bold_e_prime, bold_c}, q);

        List<BigInteger> bold_u_prime = IntStream.range(0, N)
                .mapToObj(i -> bold_u.get(psy.get(i))).collect(Collectors.toList());

        CommitmentChain commitmentChain = genCommitmentChain(h, bold_u_prime);
        List<BigInteger> bold_c_circ = commitmentChain.getBold_c();
        List<BigInteger> bold_r_circ = commitmentChain.getBold_r();

        BigInteger omega_1 = randomGenerator.randomInZq(q);
        BigInteger omega_2 = randomGenerator.randomInZq(q);
        BigInteger omega_3 = randomGenerator.randomInZq(q);
        BigInteger omega_4 = randomGenerator.randomInZq(q);

        List<BigInteger> bold_omega_circ = new ArrayList<>();
        List<BigInteger> bold_omega_prime = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            bold_omega_circ.add(randomGenerator.randomInZq(q));
            bold_omega_prime.add(randomGenerator.randomInZq(q));
        }

        ShuffleProof.T t = computeT(bold_e_prime, N, p, g, h, pk, bold_h, bold_c_circ,
                omega_1, omega_2, omega_3, omega_4, bold_omega_circ, bold_omega_prime);
        Object[] y = {bold_e, bold_e_prime, bold_c, bold_c_circ, pk};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, t.elementsToHash(), q);

        ShuffleProof.S s = computeS(bold_r_prime, N, q, bold_r, bold_u, bold_u_prime, bold_r_circ,
                omega_1, omega_2, omega_3, omega_4, bold_omega_circ, bold_omega_prime, c);

        return new ShuffleProof(t, s, bold_c, bold_c_circ);
    }

    private ShuffleProof.S computeS(List<BigInteger> bold_r_prime, int N, BigInteger q, List<BigInteger> bold_r,
                                    List<BigInteger> bold_u, List<BigInteger> bold_u_prime, List<BigInteger> bold_r_circ,
                                    BigInteger omega_1, BigInteger omega_2, BigInteger omega_3, BigInteger omega_4,
                                    List<BigInteger> bold_omega_circ, List<BigInteger> bold_omega_prime, BigInteger c) {
        BigInteger s_1 = computeS1(q, bold_r, omega_1, c);

        List<BigInteger> v = computeV(N, q, bold_u_prime);

        BigInteger s_2 = computeSi(N, q, bold_r_circ, omega_2, c, v);
        BigInteger s_3 = computeSi(N, q, bold_r, omega_3, c, bold_u);
        BigInteger s_4 = computeSi(N, q, bold_r_prime, omega_4, c, bold_u);

        List<BigInteger> s_circ = new ArrayList<>();
        List<BigInteger> s_prime = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            BigInteger s_circ_i = bold_omega_circ.get(i).add(c.multiply(bold_r_circ.get(i))).mod(q);
            s_circ.add(s_circ_i);

            BigInteger s_prime_i = bold_omega_prime.get(i).add(c.multiply(bold_u_prime.get(i))).mod(q);
            s_prime.add(s_prime_i);
        }

        return new ShuffleProof.S(s_1, s_2, s_3, s_4, s_circ, s_prime);
    }

    private BigInteger computeSi(int N, BigInteger q, List<BigInteger> bold_r_circ, BigInteger omega_2, BigInteger c, List<BigInteger> v) {
        BigInteger r_circ = IntStream.range(0, N)
                .mapToObj(i -> bold_r_circ.get(i).multiply(v.get(i)).mod(q))
                .reduce(BigInteger::add)
                .orElseThrow(exceptionSupplier())
                .mod(q);
        return omega_2.add(c.multiply(r_circ)).mod(q);
    }

    private List<BigInteger> computeV(int N, BigInteger q, List<BigInteger> bold_u_prime) {
        List<BigInteger> v = new ArrayList<>();
        v.add(BigInteger.ONE);
        for (int rev_i = 1; rev_i <= N; rev_i++) {
            // inserting always at index 0 means that index 0 always contains the previous value
            // and that values will be held in reverse order to the insertion order, which is equivalent to the
            // algorithm described
            int i = N - rev_i;
            BigInteger v_i = bold_u_prime.get(i).multiply(v.get(0)).mod(q);
            v.add(0, v_i);
        }
        return v;
    }

    private BigInteger computeS1(BigInteger q, List<BigInteger> bold_r, BigInteger omega_1, BigInteger c) {
        BigInteger r_bar = bold_r.stream()
                .reduce(BigInteger::add)
                .orElseThrow(exceptionSupplier())
                .mod(q);
        return omega_1.add(c.multiply(r_bar)).mod(q);
    }

    private ShuffleProof.T computeT(List<Encryption> bold_e_prime, int N, BigInteger p, BigInteger g, BigInteger h,
                                    BigInteger pk, List<BigInteger> bold_h, List<BigInteger> bold_c_circ,
                                    BigInteger omega_1, BigInteger omega_2, BigInteger omega_3, BigInteger omega_4,
                                    List<BigInteger> bold_omega_circ, List<BigInteger> bold_omega_prime) {
        BigInteger t_1 = g.modPow(omega_1, p);
        BigInteger t_2 = g.modPow(omega_2, p);

        BigInteger h_prod = getBoldHProduct(N, p, bold_h, bold_omega_prime);
        BigInteger t_3 = g.modPow(omega_3, p).multiply(h_prod).mod(p);

        BigInteger a_prime_prod = getAPrimeProd(bold_e_prime, N, p, bold_omega_prime);
        BigInteger t_4_1 = pk.modPow(omega_4.negate(), p).multiply(a_prime_prod).mod(p);

        BigInteger b_prime_prod = getBPrimeProd(bold_e_prime, N, p, bold_omega_prime);
        BigInteger t_4_2 = g.modPow(omega_4.negate(), p).multiply(b_prime_prod).mod(p);

        // insert c_circ_0, thus offsetting c_circ indices by 1...
        bold_c_circ.add(0, h);
        List<BigInteger> bold_t_circ = IntStream.range(0, N)
                .mapToObj(i -> g.modPow(bold_omega_circ.get(i), p)
                        .multiply(bold_c_circ.get(i).modPow(bold_omega_prime.get(i), p))
                        .mod(p)).collect(Collectors.toList());
        bold_c_circ.remove(0); // restore c_circ to its former status
        return new ShuffleProof.T(t_1, t_2, t_3, Arrays.asList(t_4_1, t_4_2), bold_t_circ);
    }

    private BigInteger getBPrimeProd(List<Encryption> bold_e_prime, int N, BigInteger p, List<BigInteger> bold_omega_prime) {
        return IntStream.range(0, N)
                .mapToObj(i -> bold_e_prime.get(i).getB().modPow(bold_omega_prime.get(i), p))
                .reduce(multiplyModP(p))
                .orElseThrow(exceptionSupplier());
    }

    private BigInteger getAPrimeProd(List<Encryption> bold_e_prime, int N, BigInteger p, List<BigInteger> bold_omega_prime) {
        return IntStream.range(0, N)
                .mapToObj(i -> bold_e_prime.get(i).getA().modPow(bold_omega_prime.get(i), p))
                .reduce(multiplyModP(p))
                .orElseThrow(exceptionSupplier());
    }

    private BinaryOperator<BigInteger> multiplyModP(BigInteger p) {
        return (a, b) -> a.multiply(b).mod(p);
    }

    private Supplier<IllegalStateException> exceptionSupplier() {
        return () -> new IllegalStateException("Should not happen, if N > 0");
    }

    private BigInteger getBoldHProduct(int n, BigInteger p, List<BigInteger> bold_h, List<BigInteger> bold_omega_prime) {
        return IntStream.range(0, n)
                .mapToObj(i -> bold_h.get(i).modPow(bold_omega_prime.get(i), p))
                .reduce(multiplyModP(p))
                .orElseThrow(exceptionSupplier());
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
     * @param c_0          initial commitment
     * @param bold_u_prime the permuted challenges
     * @return a commitment chain relative to the permuted list of public challenges
     */
    public CommitmentChain genCommitmentChain(BigInteger c_0, List<BigInteger> bold_u_prime) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();

        List<BigInteger> bold_c = new ArrayList<>();
        List<BigInteger> bold_r = new ArrayList<>();

        bold_c.add(c_0); // c_0, we'll remove it afterwards

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
