package ch.ge.ve.protopoc.service.simulation;

import ch.ge.ve.protopoc.service.algorithm.TallyingAuthoritiesAlgorithm;
import ch.ge.ve.protopoc.service.exception.InvalidDecryptionProofException;
import ch.ge.ve.protopoc.service.model.DecryptionProof;
import ch.ge.ve.protopoc.service.model.Encryption;
import ch.ge.ve.protopoc.service.model.TallyData;
import ch.ge.ve.protopoc.service.protocol.BulletinBoardService;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class simulates the actions of the election administration
 */
public class ElectionAdministrationSimulator {
    private final Logger perfLog = LoggerFactory.getLogger("PerformanceStats");
    private final int totalCandidateCount;
    private final BulletinBoardService bulletinBoardService;
    private final TallyingAuthoritiesAlgorithm tallyingAuthoritiesAlgorithm;

    public ElectionAdministrationSimulator(int totalCandidateCount, BulletinBoardService bulletinBoardService,
                                           TallyingAuthoritiesAlgorithm tallyingAuthoritiesAlgorithm) {
        this.totalCandidateCount = totalCandidateCount;
        this.bulletinBoardService = bulletinBoardService;
        this.tallyingAuthoritiesAlgorithm = tallyingAuthoritiesAlgorithm;
    }

    public List<Long> getTally() throws InvalidDecryptionProofException {
        TallyData tallyData = bulletinBoardService.getTallyData();

        List<DecryptionProof> decryptionProofs = tallyData.getDecryptionProofs();
        List<BigInteger> publicKeyShares = tallyData.getPublicKeyShares();
        List<Encryption> finalShuffle = tallyData.getFinalShuffle();
        List<List<BigInteger>> partialDecryptions = tallyData.getPartialDecryptions();
        Stopwatch decryptionProofCheckWatch = Stopwatch.createStarted();
        if (!tallyingAuthoritiesAlgorithm.checkDecryptionProofs(decryptionProofs, publicKeyShares, finalShuffle,
                partialDecryptions)) {
            throw new InvalidDecryptionProofException("An invalid decryption proof was found");
        }
        decryptionProofCheckWatch.stop();
        perfLog.info(String.format("Administration : checked decryption proofs in %dms",
                decryptionProofCheckWatch.elapsed(TimeUnit.MILLISECONDS)));

        List<BigInteger> decryptions = tallyingAuthoritiesAlgorithm.getDecryptions(finalShuffle, partialDecryptions);
        return tallyingAuthoritiesAlgorithm.getTally(decryptions, totalCandidateCount);
    }
}
