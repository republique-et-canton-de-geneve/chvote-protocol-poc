package ch.ge.ve.protopoc.service.protocol;

import ch.ge.ve.protopoc.service.exception.IncorrectConfirmationException;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.model.polynomial.Point;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Default implementation of the {@link BulletinBoardService}
 */
public class DefaultBulletinBoard implements BulletinBoardService {
    private final List<AuthorityService> authorities = new ArrayList<>();
    private final Map<Integer, EncryptionPublicKey> publicKeyParts = new HashMap<>();
    private final Map<Integer, List<Point>> publicCredentialsParts = new HashMap<>();
    private final Map<Integer, List<Encryption>> shuffles = new HashMap<>();
    private final Map<Integer, ShuffleProof> shuffleProofs = new HashMap<>();
    private final Map<Integer, List<BigInteger>> partialDecryptions = new HashMap<>();
    private final Map<Integer, DecryptionProof> decryptionProofs = new HashMap<>();
    private PublicParameters publicParameters;
    private ElectionSet electionSet;
    private List<Long> tally;

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

        return authorities.parallelStream()
                .map(authority -> authority.handleBallot(voterIndex, ballotAndQuery))
                .collect(Collectors.toList());
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

    @Override
    public void publishShuffleAndProof(int j, List<Encryption> shuffle, ShuffleProof proof) {
        Preconditions.checkElementIndex(j, publicParameters.getS(),
                "j needs to be within bounds");
        Preconditions.checkArgument(shuffles.size() == j,
                "Shuffle j can only be inserted after the previous shuffles");
        Preconditions.checkArgument(shuffleProofs.size() == j,
                "Shuffle proof j can only be inserted after the previous shuffle proof");
        shuffles.put(j, shuffle);
        shuffleProofs.put(j, proof);
    }

    @Override
    public List<Encryption> getPreviousShuffle(int j) {
        Preconditions.checkElementIndex(j, publicParameters.getS(),
                "j needs to be within bounds");
        Preconditions.checkArgument(shuffles.containsKey(j),
                "Can't retrieve a shuffle that hasn't been inserted");
        return shuffles.get(j);
    }

    @Override
    public ShufflesAndProofs getShufflesAndProofs() {
        Preconditions.checkState(shuffles.size() == publicParameters.getS(),
                "This may only happen during decryption time, once all the shuffles have been entered");
        Preconditions.checkState(shuffleProofs.size() == publicParameters.getS(),
                "This may only happen during decryption time, once all the shuffles have been entered");

        List<List<Encryption>> shuffleList = new ArrayList<>();
        List<ShuffleProof> shuffleProofList = new ArrayList<>();

        IntStream.range(0, shuffles.size()).forEach(i -> {
            shuffleList.add(shuffles.get(i));
            shuffleProofList.add(shuffleProofs.get(i));
        });
        return new ShufflesAndProofs(shuffleList, shuffleProofList);
    }

    @Override
    public void publishPartialDecryptionAndProof(int j, List<BigInteger> partialDecryption, DecryptionProof proof) {
        Preconditions.checkElementIndex(j, publicParameters.getS(),
                "j needs to be within bounds");
        Preconditions.checkState(shuffles.size() == publicParameters.getS(),
                "The decryptions may only start when all the shuffles have been published");
        Preconditions.checkState(shuffleProofs.size() == publicParameters.getS(),
                "The decryptions may only start when all the shuffles proofs have been published");
        Preconditions.checkArgument(!partialDecryptions.containsKey(j),
                "Partial decryptions may not be updated");
        Preconditions.checkArgument(!decryptionProofs.containsKey(j),
                "Partial decryptions proofs may not be updated");
        partialDecryptions.put(j, partialDecryption);
        decryptionProofs.put(j, proof);
    }

    @Override
    public TallyData getTallyData() {
        Preconditions.checkState(partialDecryptions.size() == publicParameters.getS(),
                "The tallying may only start when all the decryptions have been published");
        Preconditions.checkState(decryptionProofs.size() == publicParameters.getS(),
                "The tallying may only start when all the decryption proofs have been published");
        List<BigInteger> publicKeyShares = new ArrayList<>();
        List<Encryption> finalShuffle = shuffles.get(publicParameters.getS() - 1);
        List<List<BigInteger>> partialDecryptionsList = new ArrayList<>();
        List<DecryptionProof> decryptionProofList = new ArrayList<>();

        IntStream.range(0, publicParameters.getS())
                .forEach(i -> {
                    publicKeyShares.add(publicKeyParts.get(i).getPublicKey());
                    partialDecryptionsList.add(partialDecryptions.get(i));
                    decryptionProofList.add(decryptionProofs.get(i));
                });

        return new TallyData(publicKeyShares, finalShuffle, partialDecryptionsList, decryptionProofList);
    }

    @Override
    public void publishTally(List<Long> tally) {
        Preconditions.checkState(partialDecryptions.size() == publicParameters.getS(),
                "The tallying may only start when all the decryptions have been published");
        Preconditions.checkState(decryptionProofs.size() == publicParameters.getS(),
                "The tallying may only start when all the decryption proofs have been published");

        this.tally = tally;
    }
}
