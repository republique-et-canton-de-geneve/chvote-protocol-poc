package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.exception.NotEnoughPrimesInGroupException;
import ch.ge.ve.protopoc.service.exception.TallyingRuntimeException;
import ch.ge.ve.protopoc.service.model.DecryptionProof;
import ch.ge.ve.protopoc.service.model.Encryption;
import ch.ge.ve.protopoc.service.model.PublicParameters;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Algorithms performed during the tallying of the results
 */
public class TallyingAuthoritiesAlgorithm {
    private final static Logger log = LoggerFactory.getLogger(TallyingAuthoritiesAlgorithm.class);

    private final PublicParameters publicParameters;
    private final GeneralAlgorithms generalAlgorithms;

    public TallyingAuthoritiesAlgorithm(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms) {
        this.publicParameters = publicParameters;
        this.generalAlgorithms = generalAlgorithms;
    }

    /**
     * Algorithm 5.51: GenDecryptionProofs
     *
     * @param bold_pi_prime the vector of the decryption proofs, by authority
     * @param bold_pk       the vector of the public key shares, by authority
     * @param bold_e        the vector of the encrypted ballots
     * @param bold_B_prime  the matrix of partial decryptions, by authority, by ballot
     * @return true if all the proofs are valid, false otherwise
     */
    public boolean checkDecryptionProofs(List<DecryptionProof> bold_pi_prime, List<BigInteger> bold_pk,
                                         List<Encryption> bold_e, List<List<BigInteger>> bold_B_prime) {
        int s = publicParameters.getS();
        int N = bold_e.size();
        Preconditions.checkArgument(bold_pi_prime.size() == s,
                "There should be as many decryption proofs as authorities");
        Preconditions.checkArgument(bold_pk.size() == s,
                "There should be as many public key shares as authorities");
        Preconditions.checkArgument(bold_B_prime.size() == s,
                "There should be as many rows to bold_B_prime as there are authorities");
        Preconditions.checkArgument(bold_B_prime.stream().map(List::size).allMatch(l -> l == N),
                "There should be as many columns to bold_B_prime as there are encryptions");
        return IntStream.range(0, s).allMatch(j ->
                checkDecryptionProof(bold_pi_prime.get(j), bold_pk.get(j), bold_e, bold_B_prime.get(j)));
    }

    /**
     * Algorithm 5.52: CheckDecryptionProof
     *
     * @param pi_prime     the decryption proof
     * @param pk_j         the authority's public key
     * @param bold_e       the vector of the encryptions
     * @param bold_b_prime the vector of the partial decryptions
     * @return true if the proof is valid, false otherwise
     */
    public boolean checkDecryptionProof(DecryptionProof pi_prime, BigInteger pk_j, List<Encryption> bold_e,
                                        List<BigInteger> bold_b_prime) {
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();

        List<BigInteger> bold_b = bold_e.stream().map(Encryption::getB).collect(Collectors.toList());
        Object[] y = {pk_j, bold_b, bold_b_prime};
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, pi_prime.getT().toArray(new BigInteger[0]), q);
        BigInteger t_prime_0 = modExp(pk_j, c.negate(), p).multiply(modExp(g, pi_prime.getS(), p)).mod(p);
        List<BigInteger> t_prime = IntStream.range(0, bold_b.size())
                .mapToObj(i ->
                        modExp(bold_b_prime.get(i), c.negate(), p)
                                .multiply(modExp(bold_b.get(i), pi_prime.getS(), p)).mod(p))
                .collect(Collectors.toList());
        t_prime.add(0, t_prime_0);

        boolean isProofValid = IntStream.range(0, t_prime.size()).allMatch(i -> pi_prime.getT().get(i).compareTo(t_prime.get(i)) == 0);
        if (!isProofValid) {
            log.error("Invalid decryption proof found");
        }
        return isProofValid;
    }

    /**
     * Algorithm 5.43: GetDecryptions
     *
     * @param bold_e       the ElGamal encryptions of the ballots
     * @param bold_B_prime the matrix of partial decryptions, per authority, per ballot
     * @return the list of decryptions, by assembling the partial decryptions obtained from the authorities
     */
    public List<BigInteger> getDecryptions(List<Encryption> bold_e, List<List<BigInteger>> bold_B_prime) {
        int N = bold_e.size();
        int s = publicParameters.getS();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        Preconditions.checkArgument(bold_B_prime.size() == s,
                "There should be one row in bold_B_prime per authority");
        Preconditions.checkArgument(bold_B_prime.stream().map(List::size).allMatch(l -> l == N),
                "Each row of bold_B_prime should contain one partial decryption per ballot");
        return IntStream.range(0, N).mapToObj(i -> {
            BigInteger b_prime_i = IntStream.range(0, s).mapToObj(j -> bold_B_prime.get(j).get(i))
                    .reduce(BigInteger::multiply)
                    .orElse(ONE)
                    .mod(p);
            return bold_e.get(i).getA().multiply(b_prime_i.modInverse(p)).mod(p);
        }).collect(Collectors.toList());
    }

    /**
     * Algorithm 5.54: GetTally
     *
     * @param bold_m the products of encoded selections
     * @param n      the number of candidates
     * @return the final tally <tt>t = (t_0, ..., t_n)</tt>, where t_i is the number of votes candidate i received
     */
    public List<Long> getTally(List<BigInteger> bold_m, int n) {
        List<BigInteger> bold_p;
        try {
            bold_p = generalAlgorithms.getPrimes(n);
        } catch (NotEnoughPrimesInGroupException e) {
            log.error("Error while tallying the votes", e);
            throw new TallyingRuntimeException(e);
        }

        return bold_p.stream()
                .map(p_j -> bold_m.stream().filter(m_i -> m_i.mod(p_j).compareTo(ZERO) == 0).count())
                .collect(Collectors.toList());
    }
}
