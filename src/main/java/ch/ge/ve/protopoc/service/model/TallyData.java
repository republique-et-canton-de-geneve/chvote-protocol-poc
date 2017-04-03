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

package ch.ge.ve.protopoc.service.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class containing all of the data required for the tallying of the votes
 */
public class TallyData {
    private final List<BigInteger> publicKeyShares;
    private final List<Encryption> finalShuffle;
    private final List<List<BigInteger>> partialDecryptions;
    private final List<DecryptionProof> decryptionProofs;

    public TallyData(List<BigInteger> publicKeyShares, List<Encryption> finalShuffle, List<List<BigInteger>> partialDecryptions, List<DecryptionProof> decryptionProofs) {
        this.publicKeyShares = publicKeyShares;
        this.finalShuffle = finalShuffle;
        this.partialDecryptions = partialDecryptions;
        this.decryptionProofs = decryptionProofs;
    }

    public List<BigInteger> getPublicKeyShares() {
        return publicKeyShares;
    }

    public List<Encryption> getFinalShuffle() {
        return finalShuffle;
    }

    public List<List<BigInteger>> getPartialDecryptions() {
        return partialDecryptions;
    }

    public List<DecryptionProof> getDecryptionProofs() {
        return decryptionProofs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TallyData tallyData = (TallyData) o;
        return Objects.equals(publicKeyShares, tallyData.publicKeyShares) &&
                Objects.equals(finalShuffle, tallyData.finalShuffle) &&
                Objects.equals(partialDecryptions, tallyData.partialDecryptions) &&
                Objects.equals(decryptionProofs, tallyData.decryptionProofs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKeyShares, finalShuffle, partialDecryptions, decryptionProofs);
    }
}
