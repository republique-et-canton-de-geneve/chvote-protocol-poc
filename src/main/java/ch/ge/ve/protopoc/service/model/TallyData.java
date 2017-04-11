/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - chvote-protocol-poc                                                                            -
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

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Model class containing all of the data required for the tallying of the votes
 */
public final class TallyData {
    private final List<BigInteger> publicKeyShares;
    private final List<Encryption> finalShuffle;
    private final List<List<BigInteger>> partialDecryptions;
    private final List<DecryptionProof> decryptionProofs;

    public TallyData(List<BigInteger> publicKeyShares, List<Encryption> finalShuffle, List<List<BigInteger>> partialDecryptions, List<DecryptionProof> decryptionProofs) {
        this.publicKeyShares = ImmutableList.copyOf(publicKeyShares);
        this.finalShuffle = ImmutableList.copyOf(finalShuffle);
        this.partialDecryptions = partialDecryptions.parallelStream().map(ImmutableList::copyOf)
                .collect(Collectors.toList());
        this.decryptionProofs = ImmutableList.copyOf(decryptionProofs);
    }

    public List<BigInteger> getPublicKeyShares() {
        return ImmutableList.copyOf(publicKeyShares);
    }

    public List<Encryption> getFinalShuffle() {
        return ImmutableList.copyOf(finalShuffle);
    }

    public List<List<BigInteger>> getPartialDecryptions() {
        return partialDecryptions.parallelStream().map(ImmutableList::copyOf)
                .collect(Collectors.toList());
    }

    public List<DecryptionProof> getDecryptionProofs() {
        return ImmutableList.copyOf(decryptionProofs);
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
