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

import ch.ge.ve.protopoc.service.model.polynomial.Point;

import java.util.List;

/**
 * Model class holding data for the whole electorate
 */
public class ElectorateData {
    private final List<SecretVoterData> d;
    private final List<Point> d_circ;
    private final List<List<Point>> P;
    private final List<List<Integer>> K;

    public ElectorateData(List<SecretVoterData> secretVoterDataList, List<Point> publicVoterDataList, List<List<Point>> randomPoints, List<List<Integer>> allowedSelections) {
        this.d = secretVoterDataList;
        this.d_circ = publicVoterDataList;
        this.P = randomPoints;
        this.K = allowedSelections;
    }

    public List<SecretVoterData> getD() {
        return d;
    }

    public List<Point> getD_circ() {
        return d_circ;
    }

    public List<List<Point>> getP() {
        return P;
    }

    public List<List<Integer>> getK() {
        return K;
    }
}
