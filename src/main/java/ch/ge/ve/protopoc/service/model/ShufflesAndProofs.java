package ch.ge.ve.protopoc.service.model;

import java.util.List;

/**
 * Model class containing the shuffles (re-encrypted, shuffled ballots) and the shuffle proofs
 */
public class ShufflesAndProofs {
    private final List<List<Encryption>> shuffles;
    private final List<ShuffleProof> shuffleProofs;


    public ShufflesAndProofs(List<List<Encryption>> shuffles, List<ShuffleProof> shuffleProofs) {
        this.shuffles = shuffles;
        this.shuffleProofs = shuffleProofs;
    }

    public List<List<Encryption>> getShuffles() {
        return shuffles;
    }

    public List<ShuffleProof> getShuffleProofs() {
        return shuffleProofs;
    }
}
