/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - protocol-poc-back                                                                              -
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

import java.util.List;

/**
 * The model class grouping all public parameters
 */
public class PublicParameters {
    private final SecurityParameters securityParameters;
    private final EncryptionGroup encryptionGroup;
    private final IdentificationGroup identificationGroup;
    private final PrimeField primeField;
    /**
     * Alphabet used for voting code
     */
    private final List<Character> A_x;
    /**
     * Length of voting code l_x
     */
    private final int l_x;
    /**
     * Alphabet used for confirmation code
     */
    private final List<Character> A_y;
    /**
     * Length of confirmation code
     */
    private final int l_y;
    /**
     * Alphabet used for return codes
     */
    private final List<Character> A_r;
    /**
     * Length of return codes
     */
    private final int l_r;
    /**
     * Alphabet used for finalization codes
     */
    private final List<Character> A_f;
    /**
     * Length of finalization code
     */
    private final int l_f;
    /**
     * Length of OT messages
     */
    private final int l_m;
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
                            List<Character> A_x,
                            int l_x,
                            List<Character> A_y,
                            int l_y,
                            List<Character> A_r,
                            int l_r,
                            List<Character> A_f,
                            int l_f,
                            int l_m,
                            int s,
                            int n_max) {
        // All tests that only impact properties of a given element are performed locally at the constructor level
        // Preconditions tested here are those that impact a combination of the properties of the lower level elements
        Preconditions.checkArgument(encryptionGroup.getH().compareTo(BigIntegers.TWO) >= 0,
                "");
        Preconditions.checkArgument(2 * securityParameters.getMu() <= identificationGroup.getQ_circ().bitLength());
        Preconditions.checkArgument(2 * securityParameters.getMu() <= l_x && l_x <= identificationGroup.getQ_circ().bitLength(),
                "l_x needs to be comprised between 2*mu and the length of q_circ");
        Preconditions.checkArgument(2 * securityParameters.getMu() <= l_y && l_y <= identificationGroup.getQ_circ().bitLength(),
                "l_y needs to be comprised between 2*mu and the length of q_circ");
        Preconditions.checkArgument(l_r % 8 == 0, "l_r needs to be a multiple of 8");
        Preconditions.checkArgument(1.0 / Math.pow(2.0, l_r) <= (1.0 - securityParameters.getEpsilon()),
                "1/2^l_r must be smaller or equal to 1 - epsilon");
        Preconditions.checkArgument(l_f % 8 == 0, "l_f needs to be a multiple of 8");
        Preconditions.checkArgument(1.0 / Math.pow(2.0, l_f) <= (1.0 - securityParameters.getEpsilon()),
                "1/2^l_f must be smaller or equal to 1 - epsilon");
        Preconditions.checkArgument(l_m % 8 == 0, "l_m needs to be a multiple of 8");
        Preconditions.checkArgument(l_m == 16 * Math.ceil(primeField.getP_prime().bitLength() / 8.0),
                "l_m must be equal to 16 * (the length of p_prime / 8 rounded up)");
        Preconditions.checkArgument(s >= 1, "There must be at least one authority");
        this.securityParameters = securityParameters;
        this.encryptionGroup = encryptionGroup;
        this.identificationGroup = identificationGroup;
        this.primeField = primeField;
        this.A_x = A_x;
        this.l_x = l_x;
        this.A_y = A_y;
        this.l_y = l_y;
        this.A_r = A_r;
        this.l_r = l_r;
        this.A_f = A_f;
        this.l_f = l_f;
        this.l_m = l_m;
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

    public List<Character> getA_x() {
        return ImmutableList.copyOf(A_x);
    }

    public int getL_x() {
        return l_x;
    }

    public List<Character> getA_y() {
        return ImmutableList.copyOf(A_y);
    }

    public int getL_y() {
        return l_y;
    }

    public List<Character> getA_r() {
        return ImmutableList.copyOf(A_r);
    }

    public int getL_r() {
        return l_r;
    }

    public List<Character> getA_f() {
        return ImmutableList.copyOf(A_f);
    }

    public int getL_f() {
        return l_f;
    }

    public int getL_m() {
        return l_m;
    }

    public int getS() {
        return s;
    }

    public int getK_x() {
        return getLength(l_x, A_x.size());
    }

    public int getK_y() {
        return getLength(l_y, A_y.size());
    }

    public int getK_r() {
        return getLength(l_r, A_r.size());
    }

    public int getK_f() {
        return getLength(l_f, A_f.size());
    }

    private int getLength(int bitLength, int alphabetSize) {
        return (int) Math.ceil(bitLength / (Math.log(alphabetSize) / Math.log(2)));
    }

    public int getN_max() {
        return n_max;
    }
}
