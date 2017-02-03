package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Algorithms performed during the mixing phase, by the autorities
 */
public class MixingAuthorityAlgorithms {
    private static final Logger log = LoggerFactory.getLogger(MixingAuthorityAlgorithms.class);
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
     * Algorithm 7.40: GetEncryptions
     *
     * @param ballotList       the list of ballots submitted to the bulletin board
     * @param confirmationList the list of confirmations submitted to the bulletin board
     * @return the list of the encryptions for the valid, confirmed ballots
     */
    public List<Encryption> getEncryptions(Collection<BallotEntry> ballotList, Collection<ConfirmationEntry> confirmationList) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();

        return ballotList.stream()
                .filter(B -> voteConfirmationAuthorityAlgorithms.hasConfirmation(B.getI(), confirmationList))
                .map(B -> {
                    BigInteger a_j = B.getAlpha().getBold_a().stream()
                            .reduce(BigInteger::multiply)
                            .orElse(ONE)
                            .mod(p);
                    return new Encryption(a_j, B.getAlpha().getB());
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    /**
     * Algorithm 7.41: GenShuffle
     *
     * @param bold_e the list of ElGamal encryptions
     * @param pk     the encryption key
     * @return the result of a shuffle, with re-encryption of the values
     */
    public Shuffle genShuffle(List<Encryption> bold_e, EncryptionPublicKey pk) {
        List<Integer> psy = genPermutation(bold_e.size());

        // Parallel streams do not preserve order.
        // But it is more efficient to distribute the re-encryptions across cores and sort them than to
        // re-encrypt sequentially
        Map<Integer, ReEncryption> reEncryptionMap = IntStream.range(0, bold_e.size()).parallel().mapToObj(Integer::valueOf)
                .collect(toMap(identity(), i -> genReEncryption(bold_e.get(i), pk)));
        List<ReEncryption> reEncryptions = IntStream.range(0, bold_e.size())
                .mapToObj(reEncryptionMap::get).collect(Collectors.toList());

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
     * Algorithm 7.42: GenPermutation
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
     * Algorithm 7.43: GenReEncryption
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

        BigInteger a_prime = e.getA().multiply(modExp(pk, r_prime, p)).mod(p);
        BigInteger b_prime = e.getB().multiply(modExp(g, r_prime, p)).mod(p);

        return new ReEncryption(new Encryption(a_prime, b_prime), r_prime);
    }

    /**
     * Algorithm 7.44: GenShuffleProof
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

        List<BigInteger> bold_u_prime = psy.stream()
                .map(bold_u::get).collect(Collectors.toList());

        CommitmentChain commitmentChain = genCommitmentChain(h, bold_u_prime);
        List<BigInteger> bold_c_circ = commitmentChain.getBold_c();
        List<BigInteger> bold_r_circ = commitmentChain.getBold_r();

        BigInteger omega_1 = randomGenerator.randomInZq(q);
        BigInteger omega_2 = randomGenerator.randomInZq(q);
        BigInteger omega_3 = randomGenerator.randomInZq(q);
        BigInteger omega_4 = randomGenerator.randomInZq(q);

        List<BigInteger> bold_omega_circ = IntStream.range(0, N).parallel()
                .mapToObj(i -> randomGenerator.randomInZq(q)).collect(Collectors.toList());
        List<BigInteger> bold_omega_prime = IntStream.range(0, N).parallel()
                .mapToObj(i -> randomGenerator.randomInZq(q)).collect(Collectors.toList());

        ShuffleProof.T t = computeT(bold_e_prime, N, p, g, h, pk, bold_h, bold_c_circ,
                omega_1, omega_2, omega_3, omega_4, bold_omega_circ, bold_omega_prime);
        Object[] y = {bold_e, bold_e_prime, bold_c, bold_c_circ, pk};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, t.elementsToHash(), q);

        ShuffleProof.S s = computeS(bold_r_prime, N, q, bold_r, bold_u, bold_u_prime, bold_r_circ,
                omega_1, omega_2, omega_3, omega_4, bold_omega_circ, bold_omega_prime, c);

        log.info("Shuffle proof generated");
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
                .parallel()
                .mapToObj(i -> bold_r_circ.get(i).multiply(v.get(i)).mod(q))
                .reduce(BigInteger::add)
                .orElse(ZERO)
                .mod(q);
        return omega_2.add(c.multiply(r_circ)).mod(q);
    }

    private List<BigInteger> computeV(int N, BigInteger q, List<BigInteger> bold_u_prime) {
        List<BigInteger> v = new ArrayList<>();
        v.add(ONE);
        for (int i = N - 2; i >= 0; i--) {
            // inserting always at index 0 means that index 0 always contains the previous value
            // and that values will be held in reverse order to the insertion order, which is equivalent to the
            // algorithm described
            BigInteger v_i = bold_u_prime.get(i + 1).multiply(v.get(0)).mod(q);
            v.add(0, v_i);
        }
        return v;
    }

    private BigInteger computeS1(BigInteger q, List<BigInteger> bold_r, BigInteger omega_1, BigInteger c) {
        BigInteger r_bar = bold_r.parallelStream()
                .reduce(BigInteger::add)
                .orElse(ZERO)
                .mod(q);
        return omega_1.add(c.multiply(r_bar)).mod(q);
    }

    private ShuffleProof.T computeT(List<Encryption> bold_e_prime, int N, BigInteger p, BigInteger g, BigInteger h,
                                    BigInteger pk, List<BigInteger> bold_h, List<BigInteger> bold_c_circ,
                                    BigInteger omega_1, BigInteger omega_2, BigInteger omega_3, BigInteger omega_4,
                                    List<BigInteger> bold_omega_circ, List<BigInteger> bold_omega_prime) {
        BigInteger t_1 = modExp(g, omega_1, p);
        BigInteger t_2 = modExp(g, omega_2, p);

        BigInteger h_prod = getBoldHProduct(N, p, bold_h, bold_omega_prime);
        BigInteger t_3 = modExp(g, omega_3, p).multiply(h_prod).mod(p);

        BigInteger a_prime_prod = getAPrimeProd(bold_e_prime, N, p, bold_omega_prime);
        BigInteger t_4_1 = modExp(pk, omega_4.negate(), p).multiply(a_prime_prod).mod(p);

        BigInteger b_prime_prod = getBPrimeProd(bold_e_prime, N, p, bold_omega_prime);
        BigInteger t_4_2 = modExp(g, omega_4.negate(), p).multiply(b_prime_prod).mod(p);

        // insert c_circ_0, thus offsetting c_circ indices by 1...
        List<BigInteger> tmp_bold_c_circ = new ArrayList<>();
        tmp_bold_c_circ.add(0, h);
        tmp_bold_c_circ.addAll(bold_c_circ);

        Map<Integer, BigInteger> bold_t_circ_map = IntStream.range(0, N)
                .parallel().mapToObj(Integer::valueOf)
                .collect(toMap(identity(), i -> modExp(g, bold_omega_circ.get(i), p)
                        .multiply(modExp(tmp_bold_c_circ.get(i), bold_omega_prime.get(i), p))
                        .mod(p)));

        List<BigInteger> bold_t_circ = IntStream.range(0, N)
                .mapToObj(bold_t_circ_map::get).collect(Collectors.toList());
        tmp_bold_c_circ.remove(0); // restore c_circ to its former state
        return new ShuffleProof.T(t_1, t_2, t_3, Arrays.asList(t_4_1, t_4_2), bold_t_circ);
    }

    private BigInteger getBPrimeProd(List<Encryption> bold_e_prime, int N, BigInteger p, List<BigInteger> bold_omega_prime) {
        return IntStream.range(0, N)
                .parallel()
                .mapToObj(i -> modExp(bold_e_prime.get(i).getB(), bold_omega_prime.get(i), p))
                .reduce(multiplyMod(p))
                .orElse(ONE);
    }

    private BigInteger getAPrimeProd(List<Encryption> bold_e_prime, int N, BigInteger p, List<BigInteger> bold_omega_prime) {
        return IntStream.range(0, N)
                .parallel()
                .mapToObj(i -> modExp(bold_e_prime.get(i).getA(), bold_omega_prime.get(i), p))
                .reduce(multiplyMod(p))
                .orElse(ONE);
    }

    /**
     * Get the operator for multiplying two BigIntegers modulo a fixed one
     *
     * @param m the modulus
     * @return an operator on two BigIntegers, multiplying them modulo <tt>m</tt>
     */
    private BinaryOperator<BigInteger> multiplyMod(BigInteger m) {
        return (a, b) -> a.multiply(b).mod(m);
    }

    private BigInteger getBoldHProduct(int n, BigInteger p, List<BigInteger> bold_h, List<BigInteger> bold_omega_prime) {
        return IntStream.range(0, n)
                .parallel()
                .mapToObj(i -> modExp(bold_h.get(i), bold_omega_prime.get(i), p))
                .reduce(multiplyMod(p))
                .orElse(ONE);
    }

    public boolean checkShuffleProof(ShuffleProof pi, List<Encryption> bold_e, List<Encryption> bold_e_prime,
                                     EncryptionPublicKey publicKey) {
        int N = bold_e.size();
        List<BigInteger> bold_c = pi.getBold_c();
        List<BigInteger> bold_c_circ = pi.getBold_c_circ();
        BigInteger t_1 = pi.getT().getT_1();
        BigInteger t_2 = pi.getT().getT_2();
        BigInteger t_3 = pi.getT().getT_3();
        List<BigInteger> t_4 = pi.getT().getT_4();
        List<BigInteger> t_circ = pi.getT().getT_circ();
        BigInteger s_1 = pi.getS().getS_1();
        BigInteger s_2 = pi.getS().getS_2();
        BigInteger s_3 = pi.getS().getS_3();
        BigInteger s_4 = pi.getS().getS_4();
        List<BigInteger> s_circ = pi.getS().getS_circ();
        List<BigInteger> s_prime = pi.getS().getS_prime();
        Preconditions.checkArgument(bold_e_prime.size() == N,
                "The length of bold_e_prime should be identical to that of bold_e");
        Preconditions.checkArgument(bold_c.size() == N,
                "The length of bold_c should be identical to that of bold_e");
        Preconditions.checkArgument(bold_c_circ.size() == N,
                "The length of bold_c_circ should be identical to that of bold_e");
        Preconditions.checkArgument(t_4.size() == 2,
                "t_4 should contain two elements");
        Preconditions.checkArgument(t_circ.size() == N,
                "The length of t_circ should be identical to that of bold_e");
        Preconditions.checkArgument(s_circ.size() == N,
                "The length of s_circ should be identical to that of bold_e");
        Preconditions.checkArgument(s_prime.size() == N,
                "The length of s_prime should be identical to that of bold_e");

        BigInteger pk = publicKey.getPublicKey();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        BigInteger h = publicParameters.getEncryptionGroup().getH();

        List<BigInteger> bold_h = generalAlgorithms.getGenerators(N);
        List<BigInteger> bold_u = generalAlgorithms.getChallenges(N, new List[]{bold_e, bold_e_prime, bold_c}, q);
        Object[] y = {bold_e, bold_e_prime, bold_c, bold_c_circ, pk};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, pi.getT().elementsToHash(), q);

        BigInteger c_prod = bold_c.stream().reduce(multiplyMod(p)).orElse(ONE);
        BigInteger h_prod = bold_h.stream().reduce(multiplyMod(p)).orElse(ONE);
        BigInteger c_bar = c_prod.multiply(h_prod.modInverse(p)).mod(p);

        BigInteger u = bold_u.stream().reduce(multiplyMod(q)).orElse(ONE);

        BigInteger c_circ = bold_c_circ.get(N - 1).multiply(modExp(h, u.negate(), p));
        BigInteger c_tilde = IntStream.range(0, N).mapToObj(i -> modExp(bold_c.get(i), bold_u.get(i), p))
                .reduce(multiplyMod(p)).orElse(ONE);

        BigInteger e_prime_1 = IntStream.range(0, N).mapToObj(i -> modExp(bold_e.get(i).getA(), bold_u.get(i), p))
                .reduce(multiplyMod(p)).orElse(ONE);
        BigInteger e_prime_2 = IntStream.range(0, N).mapToObj(i -> modExp(bold_e.get(i).getB(), bold_u.get(i), p))
                .reduce(multiplyMod(p)).orElse(ONE);

        BigInteger t_prime_1 = modExp(c_bar, c.negate(), p).multiply(modExp(g, s_1, p)).mod(p);
        BigInteger t_prime_2 = modExp(c_circ, c.negate(), p).multiply(modExp(g, s_2, p)).mod(p);
        BigInteger h_i_s_prime_i = IntStream.range(0, N).parallel()
                .mapToObj(i -> modExp(bold_h.get(i), s_prime.get(i), p))
                .reduce(multiplyMod(p)).orElse(ONE);
        BigInteger t_prime_3 = modExp(c_tilde, c.negate(), p).multiply(modExp(g, s_3, p)).multiply(h_i_s_prime_i).mod(p);

        BigInteger a_prime_i_s_prime_i = IntStream.range(0, N)
                .parallel()
                .mapToObj(i -> modExp(bold_e_prime.get(i).getA(), s_prime.get(i), p))
                .reduce(multiplyMod(p)).orElse(ONE);
        BigInteger t_prime_4_1 = modExp(e_prime_1, c.negate(), p)
                .multiply(modExp(pk, s_4.negate(), p))
                .multiply(a_prime_i_s_prime_i)
                .mod(p);
        BigInteger b_prime_i_s_prime_i = IntStream.range(0, N)
                .parallel()
                .mapToObj(i -> modExp(bold_e_prime.get(i).getB(), s_prime.get(i), p))
                .reduce(multiplyMod(p)).orElse(ONE);
        BigInteger t_prime_4_2 = modExp(e_prime_2, c.negate(), p)
                .multiply(modExp(g, s_4.negate(), p))
                .multiply(b_prime_i_s_prime_i)
                .mod(p);

        // add c_circ_0: h, thus offsetting the indices for c_circ by 1.
        List<BigInteger> tmp_bold_c_circ = new ArrayList<>();
        tmp_bold_c_circ.add(0, h);
        tmp_bold_c_circ.addAll(bold_c_circ);
        Map<Integer, BigInteger> t_circ_prime_map = IntStream.range(0, N).parallel().mapToObj(Integer::valueOf)
                .collect(toMap(identity(), i -> modExp(tmp_bold_c_circ.get(i + 1), c.negate(), p)
                        .multiply(modExp(g, s_circ.get(i), p))
                        .multiply(modExp(tmp_bold_c_circ.get(i), s_prime.get(i), p))
                        .mod(p)));
        List<BigInteger> t_circ_prime = IntStream.range(0, N).mapToObj(t_circ_prime_map::get).collect(Collectors.toList());

        boolean isProofValid = t_1.compareTo(t_prime_1) == 0 &&
                t_2.compareTo(t_prime_2) == 0 &&
                t_3.compareTo(t_prime_3) == 0 &&
                t_4.get(0).compareTo(t_prime_4_1) == 0 &&
                t_4.get(1).compareTo(t_prime_4_2) == 0 &&
                IntStream.range(0, N).map(i -> t_circ.get(i).compareTo(t_circ_prime.get(i))).allMatch(i -> i == 0);
        if (!isProofValid) {
            log.error("Invalid proof found");
        }
        return isProofValid;
    }

    /**
     * Algorithm 7.46: GenPermutationCommitment
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

        // Loop indexed over j_i instead of i, for performance reasons, with a reverse permutation lookup
        List<Integer> reversePsy = reversePermutation(psy);

        Map<Integer, BigInteger> bold_r_map = IntStream.range(0, psy.size()).parallel()
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toMap(identity(), j_i -> randomGenerator.randomInZq(q)));
        Map<Integer, BigInteger> bold_c_map = IntStream.range(0, psy.size()).parallel()
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toMap(identity(), j_i -> {
                    Integer i = reversePsy.get(j_i);
                    BigInteger r_j_i = bold_r_map.get(j_i);
                    return modExp(g, r_j_i, p).multiply(bold_h.get(i)).mod(p);
                }));

        List<BigInteger> bold_c = IntStream.range(0, psy.size()).mapToObj(bold_c_map::get).collect(Collectors.toList());
        List<BigInteger> bold_r = IntStream.range(0, psy.size()).mapToObj(bold_r_map::get).collect(Collectors.toList());

        return new PermutationCommitment(bold_c, bold_r);
    }

    /**
     * Algorithm 7.47: GenCommitmentChain
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
            BigInteger c_i = modExp(g, r_i, p).multiply(modExp(c_i_minus_one, u_prime_i, p)).mod(p);

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
