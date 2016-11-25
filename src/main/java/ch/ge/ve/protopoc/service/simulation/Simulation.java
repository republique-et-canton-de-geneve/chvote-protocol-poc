package ch.ge.ve.protopoc.service.simulation;

import ch.ge.ve.protopoc.service.algorithm.*;
import ch.ge.ve.protopoc.service.model.*;
import ch.ge.ve.protopoc.service.protocol.AuthorityService;
import ch.ge.ve.protopoc.service.protocol.DefaultAuthority;
import ch.ge.ve.protopoc.service.protocol.DefaultBulletinBoard;
import ch.ge.ve.protopoc.service.protocol.DefaultVotingClient;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.Hash;
import ch.ge.ve.protopoc.service.support.JacobiSymbol;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.ge.ve.protopoc.service.support.BigIntegers.TWO;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Missing javadoc!
 */
public class Simulation {
    private final static Logger log = LoggerFactory.getLogger(Simulation.class);

    private final SecureRandom secureRandom;
    private final RandomGenerator randomGenerator;
    private PublicParameters publicParameters;
    private ElectionSet electionSet;
    private DefaultBulletinBoard bulletinBoardService;
    private KeyEstablishmentAlgorithms keyEstablishmentAlgorithms;
    private Conversion conversion;
    private Hash hash;
    private ElectionPreparationAlgorithms electionPreparationAlgorithms;
    private VoteCastingAuthorityAlgorithms voteCastingAuthorityAlgorithms;
    private GeneralAlgorithms generalAlgorithms;
    private VoteConfirmationAuthorityAlgorithms voteConfirmationAuthorityAlgorithms;
    private CodeSheetPreparationAlgorithms codeSheetPreparationAlgorithms;
    private List<AuthorityService> authorities;
    private PrintingAuthoritySimulator printingAuthoritySimulator;
    private VoteCastingClientAlgorithms voteCastingClientAlgorithms;
    private VoteConfirmationClientAlgorithms voteConfirmationClientAlgorithms;
    private VoteConfirmationVoterAlgorithms voteConfirmationVoterAlgorithms;
    private List<VoterSimulator> voterSimulators;

    public Simulation() throws NoSuchProviderException, NoSuchAlgorithmException {
        secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        randomGenerator = new RandomGenerator(secureRandom);
    }

    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException {
        log.info("Starting simulation");
        Simulation simulation = new Simulation();

        simulation.initializeSettings();
        simulation.createComponents();

        simulation.run();
    }

    private void run() {
        log.info("publishing public parameters");
        bulletinBoardService.publishPublicParameters(publicParameters);

        log.info("generating authorities keys");
        authorities.forEach(AuthorityService::generateKeys);

        log.info("building public keys");
        authorities.forEach(AuthorityService::buildPublicKey);

        log.info("publishing election set");
        bulletinBoardService.publishElectionSet(electionSet);

        log.info("generating electorate data");
        authorities.forEach(AuthorityService::generateElectorateData);

        log.info("building public credentials");
        authorities.forEach(AuthorityService::buildPublicCredentials);

        log.info("printing code sheets");
        printingAuthoritySimulator.print();

        log.info("stating the voting phase");
        voterSimulators.forEach(VoterSimulator::vote);

        log.info("all votes have been cast and confirmed");
    }

    private void createComponents() {
        log.info("creating components");
        createUtilities();

        createAlgorithms();

        createServices();

        createSimulators();
        log.info("created components");
    }

    private void createSimulators() {
        log.info("creating simulators");
        printingAuthoritySimulator = new PrintingAuthoritySimulator(bulletinBoardService, codeSheetPreparationAlgorithms);
        printingAuthoritySimulator.setAuthorities(authorities);

        voterSimulators = IntStream.range(0, electionSet.getVoters().size()).mapToObj(i ->
                new VoterSimulator(i,
                        new DefaultVotingClient(bulletinBoardService,
                                keyEstablishmentAlgorithms,
                                voteCastingClientAlgorithms,
                                voteConfirmationClientAlgorithms),
                        voteConfirmationVoterAlgorithms))
                .collect(Collectors.toList());

        printingAuthoritySimulator.setVoterSimulators(voterSimulators);
        log.info("all simulators created");
    }

    private void createServices() {
        log.info("creating services");
        bulletinBoardService = new DefaultBulletinBoard();
        authorities = IntStream.range(0, publicParameters.getS()).mapToObj(i ->
                new DefaultAuthority(i, bulletinBoardService, keyEstablishmentAlgorithms, electionPreparationAlgorithms,
                        voteCastingAuthorityAlgorithms, voteConfirmationAuthorityAlgorithms)).collect(Collectors.toList());
        bulletinBoardService.setAuthorities(authorities);
        log.info("created all services");
    }

    private void createAlgorithms() {
        log.info("instantiating algorithms classes");
        generalAlgorithms = new GeneralAlgorithms(new JacobiSymbol(), hash, conversion, publicParameters.getEncryptionGroup());
        keyEstablishmentAlgorithms = new KeyEstablishmentAlgorithms(randomGenerator);
        electionPreparationAlgorithms = new ElectionPreparationAlgorithms(hash, randomGenerator, publicParameters);
        voteCastingAuthorityAlgorithms = new VoteCastingAuthorityAlgorithms(publicParameters, generalAlgorithms, randomGenerator, hash);
        voteConfirmationAuthorityAlgorithms = new VoteConfirmationAuthorityAlgorithms(publicParameters, generalAlgorithms, voteCastingAuthorityAlgorithms, hash);
        codeSheetPreparationAlgorithms = new CodeSheetPreparationAlgorithms(publicParameters);
        voteCastingClientAlgorithms = new VoteCastingClientAlgorithms(publicParameters, hash, randomGenerator, generalAlgorithms);
        voteConfirmationClientAlgorithms = new VoteConfirmationClientAlgorithms(publicParameters, randomGenerator, generalAlgorithms, hash);
        voteConfirmationVoterAlgorithms = new VoteConfirmationVoterAlgorithms();
        log.info("instantiated all algorithm classes");
    }

    private void createUtilities() {
        conversion = new Conversion();
        hash = new Hash("SHA-512", "SUN", publicParameters.getSecurityParameters(), conversion);
    }

    private void initializeSettings() {
        log.info("Initializing settings");
        createPublicParameters();
        createElectionSet();
        log.info("Settings initialiazed");
    }

    private void createElectionSet() {
        DomainOfInfluence canton = new DomainOfInfluence("canton");
        DomainOfInfluence municipality1 = new DomainOfInfluence("municipality1");

        List<Voter> voters = IntStream.range(0, 100).mapToObj(i -> new Voter()).collect(Collectors.toList());
        voters.forEach(v -> v.addDomainsOfInfluence(canton));
        voters.subList(90, 100).forEach(v -> v.addDomainsOfInfluence(municipality1));

        Election cantonalVotation1 = new Election(3, 1, canton);
        Election cantonalVotation2 = new Election(3, 1, canton);
        Election municipalElection = new Election(10, 2, municipality1);

        List<Election> elections = Arrays.asList(cantonalVotation1, cantonalVotation2, municipalElection);

        List<Candidate> candidates = IntStream.range(0,
                cantonalVotation1.getNumberOfCandidates() +
                        cantonalVotation2.getNumberOfCandidates() +
                        municipalElection.getNumberOfCandidates()).mapToObj(
                i -> new Candidate(String.format("candidate %d", i))).collect(Collectors.toList());


        electionSet = new ElectionSet(voters, candidates, elections);
    }

    private void createPublicParameters() {
        log.info("creating public parameters");
        SecurityParameters securityParameters = new SecurityParameters(112, 112, 256, 0.999);

        EncryptionGroup encryptionGroup = createEncryptionGroup();
        IdentificationGroup identificationGroup = createIdentificationGroup(securityParameters, encryptionGroup);
        PrimeField primeField = createPrimeField(securityParameters);

        int l_m = 16 * ((int) Math.ceil(primeField.getP_prime().bitLength() / 8.0));

        publicParameters = new PublicParameters(securityParameters,
                encryptionGroup, identificationGroup, primeField,
                2 * securityParameters.mu, 2 * securityParameters.mu,
                16, 16, l_m, 4);
        log.info("public parameters created");
    }

    private PrimeField createPrimeField(SecurityParameters securityParameters) {
        log.info("creating prime field");
        PrimeField primeField = null;
        while (primeField == null) {
            BigInteger p_prime = BigInteger.probablePrime(2 * securityParameters.mu, secureRandom);
            primeField = new PrimeField(p_prime);
        }
        log.info("prime field created");
        return primeField;
    }

    private IdentificationGroup createIdentificationGroup(SecurityParameters securityParameters,
                                                          EncryptionGroup encryptionGroup) {
        log.info("creating identification group");
        IdentificationGroup identificationGroup = null;
        while (identificationGroup == null) {
            BigInteger p_circ = SimulationConstants.p_prime_2048;
            BigInteger p_circMinusOne = p_circ.subtract(ONE);

            BigInteger h = TWO;
            while (p_circMinusOne.mod(h).compareTo(ZERO) != 0) {
                h = h.add(ONE);
            }
            BigInteger q_circ = p_circMinusOne.divide(h);
            if (!q_circ.isProbablePrime(100)) {
                log.info("q_circ is not prime");
                continue;
            }
            if (q_circ.bitLength() < 2 * securityParameters.mu) {
                log.info("|q_circ| < 2*mu");
                continue;
            }

            BigInteger i = randomGenerator.randomInZq(p_circ);
            while (i.modPow(h, p_circ).compareTo(ONE) == 0) {
                i = randomGenerator.randomInZq(p_circ);
            }
            BigInteger g_circ = i.modPow(h, p_circ);

            try {
                identificationGroup = new IdentificationGroup(p_circ, q_circ, g_circ);
            } catch (IllegalArgumentException e) {
                log.warn("failed to create identification group", e);
                identificationGroup = null;
            }
        }
        log.info("created identification group");
        return identificationGroup;
    }

    private EncryptionGroup createEncryptionGroup() {
        log.info("creating encryption group");
        EncryptionGroup encryptionGroup = null;

        while (encryptionGroup == null) {
            BigInteger p = SimulationConstants.p2048;
            if (!p.isProbablePrime(100)) {
                log.info("p is not prime...");
                continue;
            }

            BigInteger pMinusOne = p.subtract(ONE);
            BigInteger q = pMinusOne.shiftRight(1);
            if (!q.isProbablePrime(100)) {
                log.info("q is not prime...");
            }

            BigInteger g = getGenerator(q, p, pMinusOne);

            BigInteger h = randomGenerator.randomInGq(new EncryptionGroup(p, q, g, TWO));

            try {
                encryptionGroup = new EncryptionGroup(p, q, g, h);
            } catch (IllegalArgumentException e) {
                log.warn("Encryption group creation failed", e);
                encryptionGroup = null;
            }
        }
        log.info("encryption group created: " + encryptionGroup);
        return encryptionGroup;
    }

    private BigInteger getGenerator(BigInteger q, BigInteger p, BigInteger pMinusOne) {
        log.info("creating a generator");
        BigInteger h = BigInteger.ONE;
        boolean safe = false;
        while (!safe) {
            h = randomGenerator.randomInZq(p);

            safe = h.modPow(TWO, p).compareTo(ONE) != 0
                    && h.modPow(q, p).compareTo(ONE) != 0
                    && pMinusOne.mod(h).compareTo(BigInteger.ZERO) != 0;

            BigInteger gInv = safe ? h.modInverse(p) : ONE;
            safe = safe && pMinusOne.mod(gInv).compareTo(BigInteger.ZERO) != 0;
        }
        log.info("generator created");
        return h.modPow(TWO, p);
    }
}
