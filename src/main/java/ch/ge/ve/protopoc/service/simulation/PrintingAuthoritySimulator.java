package ch.ge.ve.protopoc.service.simulation;

import ch.ge.ve.protopoc.service.algorithm.CodeSheetPreparationAlgorithms;
import ch.ge.ve.protopoc.service.model.CodeSheet;
import ch.ge.ve.protopoc.service.model.ElectionSet;
import ch.ge.ve.protopoc.service.model.PublicParameters;
import ch.ge.ve.protopoc.service.model.SecretVoterData;
import ch.ge.ve.protopoc.service.protocol.AuthorityService;
import ch.ge.ve.protopoc.service.protocol.BulletinBoardService;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulation class for the Printing Authority
 */
public class PrintingAuthoritySimulator {
    private final BulletinBoardService bulletinBoardService;
    private final List<AuthorityService> authorities = new ArrayList<>();
    private final List<VoterSimulator> voterSimulators = new ArrayList<>();
    private final CodeSheetPreparationAlgorithms codeSheetPreparationAlgorithms;

    public PrintingAuthoritySimulator(BulletinBoardService bulletinBoardService, CodeSheetPreparationAlgorithms codeSheetPreparationAlgorithms) {
        this.bulletinBoardService = bulletinBoardService;
        this.codeSheetPreparationAlgorithms = codeSheetPreparationAlgorithms;
    }

    public void setAuthorities(List<AuthorityService> authorities) {
        Preconditions.checkState(this.authorities.isEmpty(),
                "The authorities cannot be changed once they have been set");
        this.authorities.addAll(authorities);
    }

    public void setVoterSimulators(List<VoterSimulator> voterSimulators) {
        Preconditions.checkState(this.voterSimulators.isEmpty(),
                "The voter simulators may not be updated once set");
        this.voterSimulators.addAll(voterSimulators);
    }

    public void startPrinting() {
        PublicParameters publicParameters = bulletinBoardService.getPublicParameters();
        Preconditions.checkState(authorities.size() == publicParameters.getS(),
                "The number of authorities should match the public parameters");
        ElectionSet electionSet = bulletinBoardService.getElectionSet();
        Preconditions.checkState(voterSimulators.size() == electionSet.getVoters().size(),
                "The number of voter simulators should be equal to " +
                        "the number of voters in the election set");

        List<List<SecretVoterData>> voterDataMatrix = new ArrayList<>();
        for (AuthorityService authority : authorities) {
            voterDataMatrix.add(authority.getPrivateCredentials());
        }

        List<CodeSheet> sheets = codeSheetPreparationAlgorithms.getSheets(electionSet, voterDataMatrix);

        for (int i = 0; i < sheets.size(); i++) {
            voterSimulators.get(i).sendCodeSheet(sheets.get(i));
        }
    }
}
