package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.exception.IncompatibleParametersException;
import ch.ge.ve.protopoc.service.exception.IncorrectBallotException;
import ch.ge.ve.protopoc.service.exception.IncorrectConfirmationException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import com.google.common.base.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link BulletinBoardService}
 */
public class DefaultBulletinBoard implements BulletinBoardService {
    private final List<AuthorityService> authorities = new ArrayList<>();
    private final Map<Integer, EncryptionPublicKey> publicKeyParts = new HashMap<>();
    private final Map<Integer, List<Point>> publicCredentialsParts = new HashMap<>();
    private PublicParameters publicParameters;
    private ElectionSet electionSet;

    public void setAuthorities(List<AuthorityService> authorities) {
        Preconditions.checkState(this.authorities.isEmpty(),
                "The authorities may not change once they have been set");
        this.authorities.addAll(authorities);
    }

    @Override
    public void publishPublicParameters(PublicParameters publicParameters) {
        Preconditions.checkNotNull(publicParameters);
        Preconditions.checkState(this.publicParameters == null,
                "Once the public parameters have been set, they can no longer be changed");
        this.publicParameters = publicParameters;
    }

    @Override
    public PublicParameters getPublicParameters() {
        return publicParameters;
    }

    @Override
    public void publishKeyPart(int j, EncryptionPublicKey publicKey) {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been defined first");
        Preconditions.checkElementIndex(j, publicParameters.getS(),
                "The index j should be lower than the number of authorities");
        publicKeyParts.put(j, publicKey);
    }

    @Override
    public List<EncryptionPublicKey> getPublicKeyParts() {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been defined first");
        Preconditions.checkState(publicKeyParts.size() == publicParameters.getS(),
                "There should be as many key parts as authorities...");

        return publicKeyParts.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public void publishElectionSet(ElectionSet electionSet) {
        Preconditions.checkNotNull(electionSet, "A valid electionSet is needed");
        this.electionSet = electionSet;
    }

    @Override
    public ElectionSet getElectionSet() {
        Preconditions.checkState(electionSet != null,
                "The electionSet needs to have been defined first");
        return electionSet;
    }

    @Override
    public void publishPublicCredentials(int j, List<Point> publicCredentials) {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been defined first");
        Preconditions.checkElementIndex(j, publicParameters.getS(),
                "The index j should be lower than the number of authorities");
        publicCredentialsParts.put(j, publicCredentials);
    }

    @Override
    public List<List<Point>> getPublicCredentialsParts() {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been defined first");
        Preconditions.checkState(publicCredentialsParts.size() == publicParameters.getS(),
                "There should be as many key parts as authorities...");

        return publicCredentialsParts.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public List<ObliviousTransferResponse> publishBallot(Integer voterIndex, BallotAndQuery ballotAndQuery)
            throws IncorrectBallotOrQueryException {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been defined first");
        Preconditions.checkState(authorities.size() == publicParameters.getS(),
                "The number of authorities should match the public parameters");

        List<ObliviousTransferResponse> responses = new ArrayList<>();

        for (AuthorityService authority : authorities) {
            try {
                responses.add(authority.handleBallot(voterIndex, ballotAndQuery));
            } catch (IncompatibleParametersException | IncorrectBallotException e) {
                throw new IncorrectBallotOrQueryException(e);
            }
        }

        return responses;
    }

    @Override
    public List<FinalizationCodePart> publishConfirmation(Integer voterIndex, Confirmation confirmation)
            throws IncorrectConfirmationException {
        Preconditions.checkState(publicParameters != null,
                "The public parameters need to have been defined first");
        Preconditions.checkState(authorities.size() == publicParameters.getS(),
                "The number of authorities should match the public parameters");

        List<FinalizationCodePart> finalizationCodeParts = new ArrayList<>();

        for (int i = 0; i < authorities.size(); i++) {
            AuthorityService authority = authorities.get(i);
            finalizationCodeParts.add(authority.handleConfirmation(voterIndex, confirmation));
        }

        return finalizationCodeParts;
    }
}
