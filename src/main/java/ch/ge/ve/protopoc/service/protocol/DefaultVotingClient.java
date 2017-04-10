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

package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.algorithm.KeyEstablishmentAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.VoteCastingClientAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.VoteConfirmationClientAlgorithms;
import ch.ge.ve.protopoc.service.exception.*;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Default implementation for the {@link VotingClientService}
 */
public class DefaultVotingClient implements VotingClientService {
    private final BulletinBoardService bulletinBoardService;
    private final KeyEstablishmentAlgorithms keyEstablishmentAlgorithms;
    private final VoteCastingClientAlgorithms voteCastingClientAlgorithms;
    private final VoteConfirmationClientAlgorithms voteConfirmationClientAlgorithms;
    Stats stats = new Stats();
    private PublicParameters publicParameters;
    private ElectionSet electionSet;
    private Integer voterIndex;
    private List<BigInteger> randomizations;
    private List<Integer> voterSelectionCounts;
    private List<List<Point>> pointMatrix;

    public DefaultVotingClient(BulletinBoardService bulletinBoardService,
                               KeyEstablishmentAlgorithms keyEstablishmentAlgorithms,
                               VoteCastingClientAlgorithms voteCastingClientAlgorithms,
                               VoteConfirmationClientAlgorithms voteConfirmationClientAlgorithms) {
        this.bulletinBoardService = bulletinBoardService;
        this.keyEstablishmentAlgorithms = keyEstablishmentAlgorithms;
        this.voteCastingClientAlgorithms = voteCastingClientAlgorithms;
        this.voteConfirmationClientAlgorithms = voteConfirmationClientAlgorithms;
    }

    @Override
    public VotingPageData startVoteSession(Integer voterIndex) {
        this.voterIndex = voterIndex;
        publicParameters = bulletinBoardService.getPublicParameters();
        electionSet = bulletinBoardService.getElectionSet();

        Voter voter = electionSet.getVoters().get(voterIndex);
        voterSelectionCounts = electionSet.getElections().stream()
                .map(e -> electionSet.isEligible(voter, e) ? e.getNumberOfSelections() : 0)
                .collect(Collectors.toList());

        return new VotingPageData(voterSelectionCounts, electionSet.getBold_n());
    }

    @Override
    public List<String> sumbitVote(String identificationCredentials, List<Integer> selections) throws VoteCastingException {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been retrieved first");
        Preconditions.checkState(electionSet != null,
                "The electionSet needs to have been retrieved first");

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<EncryptionPublicKey> publicKeyParts = bulletinBoardService.getPublicKeyParts();
        EncryptionPublicKey systemPublicKey = keyEstablishmentAlgorithms.getPublicKey(publicKeyParts);

        BallotQueryAndRand ballotQueryAndRand = computeBallot(identificationCredentials, selections, systemPublicKey);
        randomizations = ballotQueryAndRand.getBold_r();
        stopwatch.stop();
        stats.voteEncodingTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        List<ObliviousTransferResponse> obliviousTransferResponses = sentBallotAndQuery(ballotQueryAndRand.getAlpha());

        stopwatch.reset().start();
        pointMatrix = computePointMatrix(selections, obliviousTransferResponses);
        List<String> returnCodes = voteCastingClientAlgorithms.getReturnCodes(selections, pointMatrix);
        stopwatch.stop();
        stats.verificationCodesComputationTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        return returnCodes;
    }

    private BallotQueryAndRand computeBallot(String identificationCredentials, List<Integer> selections, EncryptionPublicKey systemPublicKey) throws VoteCastingException {
        BallotQueryAndRand ballotQueryAndRand;
        try {
            ballotQueryAndRand =
                    voteCastingClientAlgorithms.genBallot(identificationCredentials, selections, systemPublicKey);
        } catch (IncompatibleParametersRuntimeException e) {
            throw new VoteCastingException(e);
        }
        return ballotQueryAndRand;
    }

    private List<ObliviousTransferResponse> sentBallotAndQuery(BallotAndQuery ballotAndQuery) throws VoteCastingException {
        List<ObliviousTransferResponse> obliviousTransferResponses;
        try {
            obliviousTransferResponses = bulletinBoardService.publishBallot(voterIndex, ballotAndQuery);
        } catch (IncorrectBallotOrQueryException e) {
            throw new VoteCastingException(e);
        }
        return obliviousTransferResponses;
    }

    private List<List<Point>> computePointMatrix(List<Integer> selections, List<ObliviousTransferResponse> obliviousTransferResponses) throws VoteCastingException {
        List<List<Point>> pointMatrix;
        try {
            pointMatrix = voteCastingClientAlgorithms.getPointMatrix(obliviousTransferResponses, voterSelectionCounts, selections, randomizations);
        } catch (InvalidObliviousTransferResponseException e) {
            throw new VoteCastingException(e);
        }
        return pointMatrix;
    }

    @Override
    public String confirmVote(String confirmationCredentials) throws VoteConfirmationException {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been retrieved first");
        Preconditions.checkState(electionSet != null,
                "The electionSet needs to have been retrieved first");
        Preconditions.checkState(pointMatrix != null,
                "The point matrix needs to have been computed first");

        Stopwatch stopwatch = Stopwatch.createStarted();
        Confirmation confirmation = voteConfirmationClientAlgorithms.genConfirmation(
                confirmationCredentials, pointMatrix, voterSelectionCounts);
        stopwatch.stop();
        stats.confirmationEncodingTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        List<FinalizationCodePart> finalizationCodeParts;
        try {
            finalizationCodeParts = bulletinBoardService.publishConfirmation(voterIndex, confirmation);
        } catch (IncorrectConfirmationRuntimeException e) {
            throw new VoteConfirmationException(e);
        }

        stopwatch.reset().start();
        String finalizationCode = voteConfirmationClientAlgorithms.getFinalizationCode(finalizationCodeParts);
        stopwatch.stop();
        stats.finalizationCodeComputationTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        return finalizationCode;
    }

    public Stats getStats() {
        return stats;
    }

    public class Stats {
        private long voteEncodingTime;
        private long verificationCodesComputationTime;
        private long confirmationEncodingTime;
        private long finalizationCodeComputationTime;

        public long getVoteEncodingTime() {
            return voteEncodingTime;
        }

        public long getVerificationCodesComputationTime() {
            return verificationCodesComputationTime;
        }

        public long getConfirmationEncodingTime() {
            return confirmationEncodingTime;
        }

        public long getFinalizationCodeComputationTime() {
            return finalizationCodeComputationTime;
        }
    }
}
