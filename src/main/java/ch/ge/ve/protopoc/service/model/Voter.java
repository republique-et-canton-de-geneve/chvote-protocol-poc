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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Model class for the voter
 */
public class Voter {
    private Collection<DomainOfInfluence> allowedDomainsOfInfluence = new ArrayList<>();

    public void addDomainsOfInfluence(DomainOfInfluence... domainsOfInfluence) {
        allowedDomainsOfInfluence.addAll(Arrays.asList(domainsOfInfluence));
    }

    public Collection<DomainOfInfluence> getAllowedDomainsOfInfluence() {
        return ImmutableList.copyOf(allowedDomainsOfInfluence);
    }
}

