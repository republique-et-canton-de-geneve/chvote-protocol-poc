package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.model.CodeSheet;

/**
 * This interface defines the contract for the voter
 */
public interface VoterService {
    /**
     * Send the code sheet to this voter
     * <p><strong>Note:</strong> this interface will be replaced by actual printing of code sheets and delivery by
     * postal mail in the real implementation</p>
     *
     * @param codeSheet the sheet of the codes (return code, finalization code, private credentials) for this voter
     */
    void sendCodeSheet(CodeSheet codeSheet);
}
