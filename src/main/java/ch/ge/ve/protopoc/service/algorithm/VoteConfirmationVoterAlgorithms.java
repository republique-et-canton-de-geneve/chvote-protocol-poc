package ch.ge.ve.protopoc.service.algorithm;

import java.util.Arrays;
import java.util.List;

/**
 * Algorithms for the vote confirmation phase, on the voter's end.
 * <p>
 * These will be performed by humans in the real system, and are only implemented to facilitate complete simulations.
 */
public class VoteConfirmationVoterAlgorithms {
    /**
     * Algorithm 5.29: CheckReturnCodes
     *
     * @param bold_rc       the printed return codes, received before the vote casting phase
     * @param bold_rc_prime the displayed return codes, shown during the vote casting session
     * @param bold_s        the voter's selections
     * @return true if every displayed return code matches the corresponding printed return code
     */
    public boolean checkReturnCodes(byte[][] bold_rc, byte[][] bold_rc_prime, List<Integer> bold_s) {
        for (int i = 0; i < bold_s.size(); i++) {
            // selections are 1-based
            if (!Arrays.equals(bold_rc[bold_s.get(i) - 1], bold_rc_prime[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Algorithm 5.30: CheckFinalizationCode
     *
     * @param F       the printed finalization code, received before the vote casting phase
     * @param F_prime the displayed finalization code, shown during the vote casting session
     * @return true if both finalization codes match, false otherwise
     */
    public boolean checkFinalizationCode(byte[] F, byte[] F_prime) {
        return Arrays.equals(F, F_prime);
    }
}
