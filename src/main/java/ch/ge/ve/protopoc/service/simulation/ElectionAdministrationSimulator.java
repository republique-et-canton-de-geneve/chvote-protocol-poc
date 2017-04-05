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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<List<Boolean>> votes = tallyingAuthoritiesAlgorithm.getVotes(decryptions, totalCandidateCount);
        // Additional verifications on the votes validity may be performed here.
        return IntStream.range(0, totalCandidateCount)
                .mapToLong(i -> votes.stream().filter(vote -> vote.get(i)).count())
                .boxed().collect(Collectors.toList());
    }
}
