package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.BigIntegers;
import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * Missing javadoc!
 */
public class PublicParameters {
    public final SecurityParameters securityParameters;
    public final EncryptionGroup encryptionGroup;
    public final IdentificationGroup identificationGroup;
    public final PrimeField primeField;
    /**
     * Length of voting code l_x
     */
    public final int l_x;
    /**
     * Length of confirmation code
     */
    public final int l_y;
    /**
     * Length of return codes
     */
    public final int l_r;
    /**
     * Length of finalization code
     */
    public final int l_f;
    /**
     * Length of OT messages
     */
    public final int l_m;
    /**
     * Number of authorities
     */
    public final int s;

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
        Preconditions.checkArgument(identificationGroup.q_circ.compareTo(encryptionGroup.p.subtract(BigInteger.ONE).divide(encryptionGroup.h)) == 0,
                "q_circ must equal (p - 1)/h");
        Preconditions.checkArgument(encryptionGroup.h.compareTo(BigIntegers.TWO) >= 0,
                "");
        Preconditions.checkArgument(2 * securityParameters.mu <= identificationGroup.q_circ.bitLength());
        Preconditions.checkArgument(l_x % 8 == 0, "l_x needs to be a multiple of 8");
        Preconditions.checkArgument(2 * securityParameters.mu <= l_x && l_x <= identificationGroup.q_circ.bitLength(),
                "l_x needs to be comprised between 2*mu and the length of q_circ");
        Preconditions.checkArgument(l_y % 8 == 0, "l_y needs to be a multiple of 8");
        Preconditions.checkArgument(2 * securityParameters.mu <= l_y && l_y <= identificationGroup.q_circ.bitLength(),
                "l_y needs to be comprised between 2*mu and the length of q_circ");
        Preconditions.checkArgument(l_r % 8 == 0, "l_r needs to be a multiple of 8");
        Preconditions.checkArgument(1.0/Math.pow(2.0, l_r) <= (1.0 - securityParameters.epsilon),
                "1/2^l_r must be smaller or equal to 1 - epsilon");
        Preconditions.checkArgument(l_f % 8 == 0, "l_f needs to be a multiple of 8");
        Preconditions.checkArgument(1.0/Math.pow(2.0, l_f) <= (1.0 - securityParameters.epsilon),
                "1/2^l_f must be smaller or equal to 1 - epsilon");
        Preconditions.checkArgument(l_m % 8 == 0, "l_m needs to be a multiple of 8");
        Preconditions.checkArgument(l_m == 16 * Math.ceil(primeField.p_prime.bitLength()/8.0),
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
}
