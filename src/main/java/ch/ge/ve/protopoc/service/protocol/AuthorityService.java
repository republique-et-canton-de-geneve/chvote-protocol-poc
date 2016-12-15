package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.IncorrectBallotException;
import ch.ge.ve.protopoc.service.exception.IncorrectConfirmationException;
import ch.ge.ve.protopoc.service.model.*;

import java.util.List;

/**
 * This interface defines the contract for an authority
 */
public interface AuthorityService {
    void generateKeys();

    void buildPublicKey();

    void generateElectorateData();

    List<SecretVoterData> getPrivateCredentials();

    void buildPublicCredentials();

    ObliviousTransferResponse handleBallot(Integer voterIndex, BallotAndQuery ballotAndQuery) throws IncompatibleParametersException, IncorrectBallotException;

    FinalizationCodePart handleConfirmation(Integer voterIndex, Confirmation confirmation) throws IncorrectConfirmationException;

    void startMixing();

    void mixAgain();

    void startPartialDecryption();
}
