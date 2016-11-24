package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.algorithm.KeyEstablishmentAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.VoteCastingClientAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.VoteConfirmationClientAlgorithms;
import ch.ge.ve.protopoc.service.exception.*;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation for the {@link VotingClientService}
 */
public class DefaultVotingClient implements VotingClientService {
    private final BulletinBoardService bulletinBoardService;
    private final KeyEstablishmentAlgorithms keyEstablishmentAlgorithms;
    private final VoteCastingClientAlgorithms voteCastingClientAlgorithms;
    private final VoteConfirmationClientAlgorithms voteConfirmationClientAlgorithms;
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


        return new VotingPageData(voterSelectionCounts);
    }

    @Override
    public byte[][] sumbitVote(byte[] identificationCredentials, List<Integer> selections) throws VoteCastingException {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been retrieved first");
        Preconditions.checkState(electionSet != null,
                "The electionSet needs to have been retrieved first");

        List<EncryptionPublicKey> publicKeyParts = bulletinBoardService.getPublicKeyParts();
        EncryptionPublicKey systemPublicKey = keyEstablishmentAlgorithms.getPublicKey(publicKeyParts);

        BallotQueryAndRand ballotQueryAndRand = computeBallot(identificationCredentials, selections, systemPublicKey);
        randomizations = ballotQueryAndRand.getBold_r();

        List<ObliviousTransferResponse> obliviousTransferResponses = sentBallotAndQuery(ballotQueryAndRand.getAlpha());

        pointMatrix = computePointMatrix(selections, obliviousTransferResponses);

        return voteCastingClientAlgorithms.getReturnCodes(pointMatrix);
    }

    private BallotQueryAndRand computeBallot(byte[] identificationCredentials, List<Integer> selections, EncryptionPublicKey systemPublicKey) throws VoteCastingException {
        BallotQueryAndRand ballotQueryAndRand;
        try {
            ballotQueryAndRand =
                    voteCastingClientAlgorithms.genBallot(identificationCredentials, selections, systemPublicKey);
        } catch (IncompatibleParametersException e) {
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
    public byte[] confirmVote(byte[] confirmationCredentials) throws VoteConfirmationException {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been retrieved first");
        Preconditions.checkState(electionSet != null,
                "The electionSet needs to have been retrieved first");
        Preconditions.checkState(pointMatrix != null,
                "The point matrix needs to have been computed first");

        Confirmation confirmation = voteConfirmationClientAlgorithms.genConfirmation(voterIndex,
                confirmationCredentials, pointMatrix, voterSelectionCounts);

        List<FinalizationCodePart> finalizationCodeParts;
        try {
            finalizationCodeParts = bulletinBoardService.publishConfirmation(voterIndex, confirmation);
        } catch (IncorrectConfirmationException e) {
            throw new VoteConfirmationException(e);
        }

        return voteConfirmationClientAlgorithms.getFinalizationCode(finalizationCodeParts);
    }
}
