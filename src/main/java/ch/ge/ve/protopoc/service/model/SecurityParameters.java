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

import com.google.common.base.Preconditions;

/**
 * This class defines the security parameters to be used
 */
public class SecurityParameters {
    /**
     * Minimal privacy security level &lambda;
     */
    private final int lambda;

    /**
     * Minimal integrity security level &mu;
     */
    private final int mu;

    /**
     * Output length of collision-resistant hash-function l (in bits)
     */
    private final int l;

    /**
     * Deterrence factor &epsilon; (with 0 &lt; &epsilon; &le; 1)
     */
    private final double epsilon;

    public SecurityParameters(int lambda, int mu, int l, double epsilon) {
        Preconditions.checkArgument(l % 8 == 0, "l should be a multiple of 8, so that l = 8L");
        Preconditions.checkArgument(l >= 2 * Math.max(lambda, mu));
        Preconditions.checkArgument(0.0 < epsilon && epsilon <= 1.0);
        this.lambda = lambda;
        this.mu = mu;
        this.l = l;
        this.epsilon = epsilon;
    }

    public int getLambda() {
        return lambda;
    }

    public int getMu() {
        return mu;
    }

    public int getL() {
        return l;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
