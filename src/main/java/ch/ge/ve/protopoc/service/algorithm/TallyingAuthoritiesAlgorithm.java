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

/**
 * Algorithms performed during the tallying of the results
 */
public class TallyingAuthoritiesAlgorithm {
    private static final Logger log = LoggerFactory.getLogger(TallyingAuthoritiesAlgorithm.class);

    private final PublicParameters publicParameters;
    private final GeneralAlgorithms generalAlgorithms;

    public TallyingAuthoritiesAlgorithm(PublicParameters publicParameters, GeneralAlgorithms generalAlgorithms) {
        this.publicParameters = publicParameters;
        this.generalAlgorithms = generalAlgorithms;
    }

    /**
     * Algorithm 7.51: GenDecryptionProofs
     *
     * @param bold_pi_prime      the vector of the decryption proofs, by authority
     * @param bold_pk            the vector of the public key shares, by authority
     * @param bold_e             the vector of the encrypted ballots
     * @param upper_bold_b_prime the matrix of partial decryptions, by authority, by ballot
     * @return true if all the proofs are valid, false otherwise
     */
    public boolean checkDecryptionProofs(List<DecryptionProof> bold_pi_prime, List<BigInteger> bold_pk,
                                         List<Encryption> bold_e, List<List<BigInteger>> upper_bold_b_prime) {
        // Validity checks
        Preconditions.checkArgument(bold_pi_prime.parallelStream().allMatch(pi_prime ->
                        pi_prime.getT().parallelStream().allMatch(generalAlgorithms::isMember) &&
                                generalAlgorithms.isInZ_q(pi_prime.getS())),
                "all pi_prime_i's t's should be in G_q, and s in Z_q");
        Preconditions.checkArgument(bold_pk.parallelStream().allMatch(generalAlgorithms::isMember),
                "all public key shares should be in G_q");
        Preconditions.checkArgument(bold_e.parallelStream().allMatch(e -> generalAlgorithms.isMember(e.getA()) &&
                        generalAlgorithms.isMember(e.getB())),
                "all e_i's must be in G_q^2");
        Preconditions.checkArgument(upper_bold_b_prime.parallelStream()
                        .flatMap(List::parallelStream).allMatch(generalAlgorithms::isMember),
                "all elements within upper_bold_b_prime should be in G_q");

        // Size checks
        int s = publicParameters.getS();
        int N = bold_e.size();
        Preconditions.checkArgument(bold_pi_prime.size() == s,
                "There should be as many decryption proofs as authorities");
        Preconditions.checkArgument(bold_pk.size() == s,
                "There should be as many public key shares as authorities");
        Preconditions.checkArgument(upper_bold_b_prime.size() == s,
                "There should be as many rows to upper_bold_b_prime as there are authorities");
        Preconditions.checkArgument(upper_bold_b_prime.stream().map(List::size).allMatch(l -> l == N),
                "There should be as many columns to upper_bold_b_prime as there are encryptions");
        return IntStream.range(0, s).allMatch(j ->
                checkDecryptionProof(bold_pi_prime.get(j), bold_pk.get(j), bold_e, upper_bold_b_prime.get(j)));
    }

    /**
     * Algorithm 7.52: CheckDecryptionProof
     *
     * @param pi_prime     the decryption proof
     * @param pk_j         the authority's public key
     * @param bold_e       the vector of the encryptions
     * @param bold_b_prime the vector of the partial decryptions
     * @return true if the proof is valid, false otherwise
     */
    public boolean checkDecryptionProof(DecryptionProof pi_prime, BigInteger pk_j, List<Encryption> bold_e,
                                        List<BigInteger> bold_b_prime) {
        // Validity checks
        Preconditions.checkArgument(pi_prime.getT().parallelStream().allMatch(generalAlgorithms::isMember),
                "all pi.t elements must be in G_q");
        Preconditions.checkArgument(generalAlgorithms.isInZ_q(pi_prime.getS()),
                "pi.s must be in Z_q");
        Preconditions.checkArgument(generalAlgorithms.isMember(pk_j),
                "the public key must be in G_q");
        Preconditions.checkArgument(bold_b_prime.parallelStream().allMatch(generalAlgorithms::isMember),
                "all elements of bold_b_prime must be in G_q");
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        BigInteger q = publicParameters.getEncryptionGroup().getQ();
        BigInteger g = publicParameters.getEncryptionGroup().getG();
        int tau = publicParameters.getSecurityParameters().getTau();

        List<BigInteger> bold_b = bold_e.stream().map(Encryption::getB).collect(Collectors.toList());
        Object[] y = {pk_j, bold_b, bold_b_prime};
        BigInteger[] t = pi_prime.getT().toArray(new BigInteger[0]);
        BigInteger c = generalAlgorithms.getNIZKPChallenge(y, t, tau);
        BigInteger t_prime_0 = modExp(pk_j, c.negate(), p).multiply(modExp(g, pi_prime.getS(), p)).mod(p);
        List<BigInteger> t_prime = IntStream.range(0, bold_b.size())
                .mapToObj(i ->
                        modExp(bold_b_prime.get(i), c.negate(), p)
                                .multiply(modExp(bold_b.get(i), pi_prime.getS(), p)).mod(p))
                .collect(Collectors.toList());
        t_prime.add(0, t_prime_0);

        boolean isProofValid = IntStream.range(0, t_prime.size()).allMatch(i ->
                pi_prime.getT().get(i).compareTo(t_prime.get(i)) == 0);
        if (!isProofValid) {
            log.error("Invalid decryption proof found");
        }
        return isProofValid;
    }

    /**
     * Algorithm 7.53: GetDecryptions
     *
     * @param bold_e             the ElGamal encryptions of the ballots
     * @param upper_bold_b_prime the matrix of partial decryptions, per authority, per ballot
     * @return the list of decryptions, by assembling the partial decryptions obtained from the authorities
     */
    public List<BigInteger> getDecryptions(List<Encryption> bold_e, List<List<BigInteger>> upper_bold_b_prime) {
        // Validity checks
        Preconditions.checkArgument(bold_e.parallelStream().allMatch(e -> generalAlgorithms.isMember(e.getA()) &&
                        generalAlgorithms.isMember(e.getB())),
                "all e_i's must be in G_q^2");
        Preconditions.checkArgument(upper_bold_b_prime.parallelStream()
                        .flatMap(List::parallelStream).allMatch(generalAlgorithms::isMember),
                "all elements within upper_bold_b_prime should be in G_q");

        // Size checks
        int N = bold_e.size();
        int s = publicParameters.getS();
        BigInteger p = publicParameters.getEncryptionGroup().getP();
        Preconditions.checkArgument(upper_bold_b_prime.size() == s,
                "There should be one row in upper_bold_b_prime per authority");
        Preconditions.checkArgument(upper_bold_b_prime.stream().map(List::size).allMatch(l -> l == N),
                "Each row of upper_bold_b_prime should contain one partial decryption per ballot");
        return IntStream.range(0, N).mapToObj(i -> {
            BigInteger b_prime_i = IntStream.range(0, s).mapToObj(j -> upper_bold_b_prime.get(j).get(i))
                    .reduce(BigInteger::multiply)
                    .orElse(ONE)
                    .mod(p);
            return bold_e.get(i).getA().multiply(b_prime_i.modInverse(p)).mod(p);
        }).collect(Collectors.toList());
    }

    /**
     * Algorithm 7.54: GetVotes
     *
     * @param bold_m the products of encoded selections
     * @param n      the number of candidates
     * @return the election result matrix upper_bold_v, where each resulting vector v_i represents somebody’s vote,
     * and each value v_{ij} = 1 represents somebody’s vote for a specific candidate j &isin; {1, ..., n}
     */
    public List<List<Boolean>> getVotes(List<BigInteger> bold_m, int n) {
        Preconditions.checkArgument(bold_m.parallelStream().allMatch(generalAlgorithms::isMember),
                "all m_i's must be in G_q");
        Preconditions.checkArgument(n >= 2, "There must be at least two candidates");
        List<BigInteger> bold_p;
        try {
            bold_p = generalAlgorithms.getPrimes(n);
        } catch (NotEnoughPrimesInGroupException e) {
            log.error("Error while tallying the votes", e);
            throw new TallyingRuntimeException(e);
        }

        return bold_m.stream()
                .map(m_i -> bold_p.stream()
                        .map(p_j -> m_i.mod(p_j).compareTo(BigInteger.ZERO) == 0).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
