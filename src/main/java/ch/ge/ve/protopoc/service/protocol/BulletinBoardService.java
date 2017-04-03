/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - protocol-poc-back                                                                              -
 - %%                                                                                             -
 - Copyright (C) 2016 - 2017 République et Canton de Genève                                       -
 - %%                                                                                             -
 - This program is free software: you can redistribute it and/or modify                           -
 - it under the terms of the GNU Affero General Public License as published by                    -
 - the Free Software Foundation, either version 3 of the License, or                              -
 - (at your option) any later version.                                                            -
 -                                                                                                -
 - This program is distributed in the hope that it will be useful,                                -
 - but WITHOUT ANY WARRANTY; without even the implied warranty of                                 -
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                                   -
 - GNU General Public License for more details.                                                   -
 -                                                                                                -
 - You should have received a copy of the GNU Affero General Public License                       -
 - along with this program. If not, see <http://www.gnu.org/licenses/>.                           -
 - #L%                                                                                            -
 -------------------------------------------------------------------------------------------------*/

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
