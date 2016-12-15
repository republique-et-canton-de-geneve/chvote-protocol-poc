package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.exception.IncorrectConfirmationException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;

import java.math.BigInteger;
import java.util.List;

/**
 * This interface defines the contract for the ballot board
 */
public interface BulletinBoardService {
    void publishPublicParameters(PublicParameters publicParameters);

    PublicParameters getPublicParameters();

    void publishKeyPart(int j, EncryptionPublicKey publicKey);

    List<EncryptionPublicKey> getPublicKeyParts();

    void publishElectionSet(ElectionSet electionSet);

    ElectionSet getElectionSet();

    void publishPublicCredentials(int j, List<Point> publicCredentials);

    List<List<Point>> getPublicCredentialsParts();

    List<ObliviousTransferResponse> publishBallot(Integer voterIndex, BallotAndQuery ballotAndQuery) throws IncorrectBallotOrQueryException;

    List<FinalizationCodePart> publishConfirmation(Integer voterIndex, Confirmation confirmation) throws IncorrectConfirmationException;

    void publishShuffleAndProof(int j, List<Encryption> shuffle, ShuffleProof proof);

    List<Encryption> getPreviousShuffle(int j);

    ShufflesAndProofs getShufflesAndProofs();

    void publishPartialDecryptionAndProof(int j, List<BigInteger> partialDecryption, DecryptionProof proof);

    TallyData getTallyData();

    void publishTally(List<Long> tally);
}
