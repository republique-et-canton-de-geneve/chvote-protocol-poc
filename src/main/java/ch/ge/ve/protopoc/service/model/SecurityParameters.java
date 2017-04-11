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

import com.google.common.base.Preconditions;

/**
 * This class defines the security parameters to be used
 */
public final class SecurityParameters {
    /**
     * Minimal privacy security level &sigma;
     */
    private final int sigma;

    /**
     * Minimal integrity security level &tau;
     */
    private final int tau;

    /**
     * Output length of collision-resistant hash-function upper_l (in bytes)
     */
    private final int upper_l;

    /**
     * Deterrence factor &epsilon; (with 0 &lt; &epsilon; &le; 1)
     */
    private final double epsilon;

    public SecurityParameters(int sigma, int tau, int upper_l, double epsilon) {
        Preconditions.checkArgument(upper_l >= Math.max(sigma, tau) / 4.0);
        Preconditions.checkArgument(0.0 < epsilon && epsilon <= 1.0);
        this.sigma = sigma;
        this.tau = tau;
        this.upper_l = upper_l;
        this.epsilon = epsilon;
    }

    public int getSigma() {
        return sigma;
    }

    public int getTau() {
        return tau;
    }

    public int getUpper_l() {
        return upper_l;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
