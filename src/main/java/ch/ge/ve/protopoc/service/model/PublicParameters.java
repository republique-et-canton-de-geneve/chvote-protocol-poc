package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.BigIntegers;
import com.google.common.base.Preconditions;

/**
 * The model class grouping all public parameters
 */
public class PublicParameters {
    private final SecurityParameters securityParameters;
    private final EncryptionGroup encryptionGroup;
    private final IdentificationGroup identificationGroup;
    private final PrimeField primeField;
    /**
     * Length of voting code l_x
     */
    private final int l_x;
    /**
     * Length of confirmation code
     */
    private final int l_y;
    /**
     * Length of return codes
     */
    private final int l_r;
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

    public PublicParameters(SecurityParameters securityParameters,
                            EncryptionGroup encryptionGroup,
                            IdentificationGroup identificationGroup,
                            PrimeField primeField,
                            int l_x,
                            int l_y,
                            int l_r,
                            int l_f,
                            int l_m,
                            int s) {
        // All tests that only impact properties of a given element are performed locally at the constructor level
        // Preconditions tested here are those that impact a combination of the properties of the lower level elements
        Preconditions.checkArgument(encryptionGroup.getH().compareTo(BigIntegers.TWO) >= 0,
                "");
        Preconditions.checkArgument(2 * securityParameters.mu <= identificationGroup.getQ_circ().bitLength());
        Preconditions.checkArgument(2 * securityParameters.mu <= l_x && l_x <= identificationGroup.getQ_circ().bitLength(),
                "l_x needs to be comprised between 2*mu and the length of q_circ");
        Preconditions.checkArgument(2 * securityParameters.mu <= l_y && l_y <= identificationGroup.getQ_circ().bitLength(),
                "l_y needs to be comprised between 2*mu and the length of q_circ");
        Preconditions.checkArgument(l_r % 8 == 0, "l_r needs to be a multiple of 8");
        Preconditions.checkArgument(1.0/Math.pow(2.0, l_r) <= (1.0 - securityParameters.epsilon),
                "1/2^l_r must be smaller or equal to 1 - epsilon");
        Preconditions.checkArgument(l_f % 8 == 0, "l_f needs to be a multiple of 8");
        Preconditions.checkArgument(1.0/Math.pow(2.0, l_f) <= (1.0 - securityParameters.epsilon),
                "1/2^l_f must be smaller or equal to 1 - epsilon");
        Preconditions.checkArgument(l_m % 8 == 0, "l_m needs to be a multiple of 8");
        Preconditions.checkArgument(l_m == 16 * Math.ceil(primeField.getP_prime().bitLength() / 8.0),
                "l_m must be equal to 16 * (the length of p_prime / 8 rounded up)");
        Preconditions.checkArgument(s >= 1, "There must be at least one authority");
        this.securityParameters = securityParameters;
        this.encryptionGroup = encryptionGroup;
        this.identificationGroup = identificationGroup;
        this.primeField = primeField;
        this.l_x = l_x;
        this.l_y = l_y;
        this.l_r = l_r;
        this.l_f = l_f;
        this.l_m = l_m;
        this.s = s;
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

    public int getL_x() {
        return l_x;
    }

    public int getL_y() {
        return l_y;
    }

    public int getL_r() {
        return l_r;
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
}
