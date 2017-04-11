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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class containing the shuffles (re-encrypted, shuffled ballots) and the shuffle proofs
 */
public final class ShufflesAndProofs {
    private final List<List<Encryption>> shuffles;
    private final List<ShuffleProof> shuffleProofs;


    public ShufflesAndProofs(List<List<Encryption>> shuffles, List<ShuffleProof> shuffleProofs) {
        this.shuffles = shuffles.parallelStream().map(ImmutableList::copyOf).collect(Collectors.toList());
        this.shuffleProofs = ImmutableList.copyOf(shuffleProofs);
    }

    public List<List<Encryption>> getShuffles() {
        return shuffles.parallelStream().map(ImmutableList::copyOf).collect(Collectors.toList());
    }

    public List<ShuffleProof> getShuffleProofs() {
        return ImmutableList.copyOf(shuffleProofs);
    }
}
