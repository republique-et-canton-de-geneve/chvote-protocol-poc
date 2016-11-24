package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.algorithm.ElectionPreparationAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.KeyEstablishmentAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.VoteCastingAuthorityAlgorithms;
import ch.ge.ve.protopoc.service.algorithm.VoteConfirmationAuthorityAlgorithms;
import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.IncorrectBallotException;
import ch.ge.ve.protopoc.service.exception.IncorrectConfirmationException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link AuthorityService} interface
 */
public class DefaultAuthority implements AuthorityService {
    private final int j;
    private final BulletinBoardService bulletinBoardService;
    private final KeyEstablishmentAlgorithms keyEstablishmentAlgorithms;
    private final ElectionPreparationAlgorithms electionPreparationAlgorithms;
    private final VoteCastingAuthorityAlgorithms voteCastingAuthorityAlgorithms;
    private final VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms;
    private EncryptionPublicKey myPublicKey;
    private EncryptionPrivateKey myPrivateKey;
    private EncryptionPublicKey systemPublicKey;
    private PublicParameters publicParameters;
    private ElectionSet electionSet;
    private ElectorateData electorateData;
    private List<Point> publicCredentials;
    private List<BallotEntry> ballotEntries = new ArrayList<>();
    private List<ConfirmationEntry> confirmationEntries = new ArrayList<>();

    public DefaultAuthority(int j, BulletinBoardService bulletinBoardService,
                            KeyEstablishmentAlgorithms keyEstablishmentAlgorithms,
                            ElectionPreparationAlgorithms electionPreparationAlgorithms,
                            VoteCastingAuthorityAlgorithms voteCastingAuthorityAlgorithms,
                            VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms) {
        this.j = j;
        this.bulletinBoardService = bulletinBoardService;
        this.keyEstablishmentAlgorithms = keyEstablishmentAlgorithms;
        this.electionPreparationAlgorithms = electionPreparationAlgorithms;
        this.voteCastingAuthorityAlgorithms = voteCastingAuthorityAlgorithms;
        this.voteConfirmationAuthorityAlgorithms = voteConfirmationAuthorityAlgorithms;
    }

    @Override
    public void generateKeys() {
        publicParameters = bulletinBoardService.getPublicParameters();
        KeyPair keyPair = keyEstablishmentAlgorithms.generateKeyPair(publicParameters.getEncryptionGroup());
        myPrivateKey = ((EncryptionPrivateKey) keyPair.getPrivate());
        myPublicKey = ((EncryptionPublicKey) keyPair.getPublic());
        bulletinBoardService.publishKeyPart(j, myPublicKey);
    }

    @Override
    public void buildPublicKey() {
        Preconditions.checkState(myPrivateKey != null,
                "The encryption keys need to have been generated beforehand");
        Preconditions.checkState(myPublicKey != null,
                "The encryption keys need to have been generated beforehand");

        List<EncryptionPublicKey> publicKeyParts = bulletinBoardService.getPublicKeyParts();
        Preconditions.checkArgument(publicKeyParts.size() == publicParameters.getS());
        Preconditions.checkArgument(publicKeyParts.get(j).equals(myPublicKey));

        systemPublicKey = keyEstablishmentAlgorithms.getPublicKey(publicKeyParts);
    }

    @Override
    public void generateElectorateData() {
        electionSet = bulletinBoardService.getElectionSet();
        electorateData = electionPreparationAlgorithms.genElectorateData(electionSet);

        bulletinBoardService.publishPublicCredentials(j, electorateData.getD_circ());
    }

    @Override
    public List<SecretVoterData> getPrivateCredentials() {
        Preconditions.checkState(electorateData != null,
                "The electorate data should have been generated first");
        return electorateData.getD();
    }

    @Override
    public void buildPublicCredentials() {
        List<List<Point>> publicCredentialsParts = bulletinBoardService.getPublicCredentialsParts();
        publicCredentials = electionPreparationAlgorithms.getPublicCredentials(publicCredentialsParts);
    }

    @Override
    public ObliviousTransferResponse handleBallot(Integer voterIndex, BallotAndQuery ballotAndQuery)
            throws IncompatibleParametersException, IncorrectBallotException {
        Preconditions.checkState(publicCredentials != null,
                "The public credentials need to have been retrieved first");
        List<BigInteger> publicIdentificationCredentials =
                publicCredentials.stream().map(p -> p.x).collect(Collectors.toList());
        if (!voteCastingAuthorityAlgorithms.checkBallot(voterIndex, ballotAndQuery, systemPublicKey,
                publicIdentificationCredentials, ballotEntries)) {

            throw new IncorrectBallotException(String.format("Ballot for voter %d was deemed invalid", voterIndex));
        }

        ObliviousTransferResponseAndRand responseAndRand =
                voteCastingAuthorityAlgorithms.genResponse(voterIndex, ballotAndQuery.getBold_a(), systemPublicKey,
                        electionSet.getBold_n(), electorateData.getK(), electorateData.getP());
        ballotEntries.add(new BallotEntry(voterIndex, ballotAndQuery, responseAndRand.getBold_r()));
        return responseAndRand.getBeta();
    }

    @Override
    public FinalizationCodePart handleConfirmation(Integer voterIndex, Confirmation confirmation)
            throws IncorrectConfirmationException {
        Preconditions.checkState(publicCredentials != null,
                "The public credentials need to have been retrieved first");
        List<BigInteger> publicConfirmationCredentials =
                publicCredentials.stream().map(p -> p.y).collect(Collectors.toList());

        if (!voteConfirmationAuthorityAlgorithms.checkConfirmation(voterIndex, confirmation,
                publicConfirmationCredentials, ballotEntries, confirmationEntries)) {
            throw new IncorrectConfirmationException("Confirmation for voter " + voterIndex + " was deemed invalid");
        }

        confirmationEntries.add(new ConfirmationEntry(voterIndex, confirmation));

        return voteConfirmationAuthorityAlgorithms.getFinalization(voterIndex, electorateData.getP(), ballotEntries);
    }
}
