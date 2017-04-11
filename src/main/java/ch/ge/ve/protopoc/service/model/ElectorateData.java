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

import ch.ge.ve.protopoc.service.model.polynomial.Point;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Model class holding data for the whole electorate
 */
public final class ElectorateData {
    private final List<SecretVoterData> d;
    private final List<Point> d_hat;
    private final List<List<Point>> P;
    private final List<List<Integer>> K;

    public ElectorateData(List<SecretVoterData> secretVoterDataList, List<Point> publicVoterDataList, List<List<Point>> randomPoints, List<List<Integer>> allowedSelections) {
        this.d = ImmutableList.copyOf(secretVoterDataList);
        this.d_hat = ImmutableList.copyOf(publicVoterDataList);
        this.P = ImmutableList.copyOf(randomPoints);
        this.K = ImmutableList.copyOf(allowedSelections);
    }

    public List<SecretVoterData> getD() {
        return ImmutableList.copyOf(d);
    }

    public List<Point> getD_hat() {
        return ImmutableList.copyOf(d_hat);
    }

    public List<List<Point>> getP() {
        return ImmutableList.copyOf(P);
    }

    public List<List<Integer>> getK() {
        return ImmutableList.copyOf(K);
    }
}
