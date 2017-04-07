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

package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.BigIntegers;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;

import static ch.ge.ve.protopoc.service.support.MoreMath.log2;

/**
 * The model class grouping all public parameters
 */
public class PublicParameters {
    private final SecurityParameters securityParameters;
    private final EncryptionGroup encryptionGroup;
    private final IdentificationGroup identificationGroup;
    private final PrimeField primeField;
    /**
     * Upper bound of secret voting credential x
     */
    private final BigInteger q_circ_x;
    /**
     * Alphabet used for voting code
     */
    private final List<Character> upper_a_x;
    /**
     * Length of voting code l_x
     */
    private final int l_x;
    /**
     * Upper bound of secret confirmation credential y
     */
    private final BigInteger q_circ_y;
    /**
     * Alphabet used for confirmation code
     */
    private final List<Character> upper_a_y;
    /**
     * Length of confirmation code
     */
    private final int l_y;
    /**
     * Alphabet used for return codes
     */
    private final List<Character> upper_a_r;
    /**
     * Length of return codes
     */
    private final int upper_l_r;
    /**
     * Length of return codes RC_{ij} (characters)
     */
    private final int l_r;
    /**
     * Alphabet used for finalization codes
     */
    private final List<Character> upper_a_f;
    /**
     * Length of finalization code
     */
    private final int upper_l_f;
    /**
     * Length of finalization codes FC_i (characters)
     */
    private final int l_f;
    /**
     * Length of OT messages
     */
    private final int upper_l_m;

    /**
     * Number of authorities
     */
    private final int s;
    /**
     * Maximal number of accepted candidates
     */
    private final int n_max;

    public PublicParameters(SecurityParameters securityParameters,
                            EncryptionGroup encryptionGroup,
                            IdentificationGroup identificationGroup,
                            PrimeField primeField,
                            BigInteger q_circ_x,
                            List<Character> upper_a_x,
                            BigInteger q_circ_y,
                            List<Character> upper_a_y,
                            List<Character> upper_a_r,
                            int upper_l_r,
                            List<Character> upper_a_f,
                            int upper_l_f,
                            int s,
                            int n_max) {
        // All tests that only impact properties of a given element are performed locally at the constructor level
        // Preconditions tested here are those that impact a combination of the properties of the lower level elements
        Preconditions.checkArgument(encryptionGroup.getH().compareTo(BigIntegers.TWO) >= 0,
                "");
        Preconditions.checkArgument(2 * securityParameters.getTau() <= identificationGroup.getQ_circ().bitLength());
        Preconditions.checkArgument(s >= 1, "There must be at least one authority");
        Preconditions.checkArgument(q_circ_x.bitLength() >= 2 * securityParameters.getTau(),
                "q_circ_x must be >= 2 * tau");
        Preconditions.checkArgument(q_circ_x.compareTo(identificationGroup.getQ_circ()) <= 0,
                "q_circ_x must be <= q_circ");
        Preconditions.checkArgument(upper_a_x.size() >= 2, "|upper_a_x| >= 2");
        Preconditions.checkArgument(q_circ_y.bitLength() >= 2 * securityParameters.getTau(),
                "q_circ_y must be >= 2 * tau");
        Preconditions.checkArgument(q_circ_y.compareTo(identificationGroup.getQ_circ()) <= 0,
                "q_circ_y must be <= q_circ");
        Preconditions.checkArgument(upper_a_y.size() >= 2, "|upper_a_y| >= 2");
        Preconditions.checkArgument(n_max >= 2, "n_max >= 2");
        Preconditions.checkArgument(8 * upper_l_r >= Math.log((n_max - 1) / (1.0 - securityParameters.getEpsilon())),
                "8 * upper_l_r >= log( ( n_max - 1 ) / ( 1 - epsilon) )");
        Preconditions.checkArgument(upper_a_r.size() >= 2, "|upper_a_r| >= 2");
        Preconditions.checkArgument(8 * upper_l_f >= Math.log(1 / (1.0 - securityParameters.getEpsilon())),
                "8 * upper_l_f >= log( 1 / ( 1 - epsilon) )");
        this.securityParameters = securityParameters;
        this.encryptionGroup = encryptionGroup;
        this.identificationGroup = identificationGroup;
        this.primeField = primeField;
        this.q_circ_x = q_circ_x;
        this.upper_a_x = upper_a_x;
        this.l_x = (int) Math.ceil(q_circ_x.bitLength() / log2(upper_a_x.size()));
        this.q_circ_y = q_circ_y;
        this.upper_a_y = upper_a_y;
        this.l_y = (int) Math.ceil(q_circ_y.bitLength() / log2(upper_a_y.size()));
        this.upper_a_r = upper_a_r;
        this.upper_l_r = upper_l_r;
        this.l_r = (int) Math.ceil((8.0 * upper_l_r) / log2(upper_a_r.size()));
        this.upper_a_f = upper_a_f;
        this.upper_l_f = upper_l_f;
        this.l_f = (int) Math.ceil((8.0 * upper_l_f) / log2(upper_a_f.size()));
        this.upper_l_m = 2 * ((int) Math.ceil(primeField.getP_prime().bitLength() / 8.0));
        this.s = s;
        this.n_max = n_max;
    }

    public SecurityParameters getSecurityParameters() {
        return securityParameters;
    }

    public EncryptionGroup getEncryptionGroup() {
        return encryptionGroup;
    }

    public IdentificationGroup getIdentificationGroup() {
        return identificationGroup;
    }

    public PrimeField getPrimeField() {
        return primeField;
    }

    public BigInteger getQ_circ_x() {
        return q_circ_x;
    }

    public List<Character> getUpper_a_x() {
        return ImmutableList.copyOf(upper_a_x);
    }

    public int getL_x() {
        return l_x;
    }

    public BigInteger getQ_circ_y() {
        return q_circ_y;
    }

    public List<Character> getUpper_a_y() {
        return ImmutableList.copyOf(upper_a_y);
    }

    public int getL_y() {
        return l_y;
    }

    public List<Character> getUpper_a_r() {
        return ImmutableList.copyOf(upper_a_r);
    }

    public int getUpper_l_r() {
        return upper_l_r;
    }

    public int getL_r() {
        return l_r;
    }

    public List<Character> getUpper_a_f() {
        return ImmutableList.copyOf(upper_a_f);
    }

    public int getUpper_l_f() {
        return upper_l_f;
    }

    public int getL_f() {
        return l_f;
    }

    public int getUpper_l_m() {
        return upper_l_m;
    }

    public int getS() {
        return s;
    }

    public int getN_max() {
        return n_max;
    }
}
