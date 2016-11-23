package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;

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

    ObliviousTransferResponse publishBallot(BallotAndQuery ballotAndQuery);

    byte[] publishConfirmation(Confirmation confirmation);
}
