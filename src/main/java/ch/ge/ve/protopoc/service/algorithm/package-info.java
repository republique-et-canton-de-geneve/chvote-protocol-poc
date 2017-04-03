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

/**
 * This package contains all the algorithms described in the specification.
 * <p>
 * <ul>
 * <li>Algorithms 7.1-7.5 are implemented in class {@link ch.ge.ve.protopoc.service.algorithm.GeneralAlgorithms}</li>
 * <li>Algorithms 7.6 and 7.10-7.12 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.ElectionPreparationAlgorithms}</li>
 * <li>Algorithms 7.7-7.9 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.PolynomialAlgorithms}</li>
 * <li>Algorithms 7.13 and 7.14 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.CodeSheetPreparationAlgorithms}</li>
 * <li>Algorithms 7.15 and 7.16 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.KeyEstablishmentAlgorithms}</li>
 * <li>Algorithm 7.17 is not included, since it only handles display to the voter which makes no sense in a
 * simulation setting, it will be handled by the front-end.</li>
 * <li>Algorithms 7.18-7.21 and 7.26-7.28 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.VoteCastingClientAlgorithms}</li>
 * <li>Algorithms 7.22-7.25 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.VoteCastingAuthorityAlgorithms}</li>
 * <li>Algorithms 7.29 and 7.39 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.VoteConfirmationVoterAlgorithms}</li>
 * <li>Algorithms 7.30-7.33 and 7.38 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.VoteConfirmationClientAlgorithms}</li>
 * <li>Algorithms 7.34-7.37 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.VoteConfirmationAuthorityAlgorithms}</li>
 * <li>Algorithms 7.40-7.47 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.MixingAuthorityAlgorithms}</li>
 * <li>Algorithms 7.48-7.50 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.DecryptionAuthorityAlgorithms}</li>
 * <li>Algorithms 7.51-7.54 are implemented in class
 * {@link ch.ge.ve.protopoc.service.algorithm.TallyingAuthoritiesAlgorithm}</li>
 * </ul>
 */
package ch.ge.ve.protopoc.service.algorithm;