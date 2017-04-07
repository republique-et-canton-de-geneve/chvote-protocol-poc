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

package ch.ge.ve.protopoc.service.model.polynomial;

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Model class combining a list of points and y-images of 0 for a set of polynomials
 */
public class PointsAndZeroImages {
    private final List<Point> points;
    private final List<BigInteger> y0s;

    public PointsAndZeroImages(List<Point> points, List<BigInteger> y0s) {
        this.points = ImmutableList.copyOf(points);
        this.y0s = ImmutableList.copyOf(y0s);
    }

    public List<Point> getPoints() {
        return ImmutableList.copyOf(points);
    }

    public List<BigInteger> getY0s() {
        return ImmutableList.copyOf(y0s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointsAndZeroImages that = (PointsAndZeroImages) o;
        return Objects.equals(points, that.points) &&
                Objects.equals(y0s, that.y0s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, y0s);
    }
}
