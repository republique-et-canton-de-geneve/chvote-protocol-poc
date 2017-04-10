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

import ch.ge.ve.protopoc.service.support.Conversion;

import java.math.BigInteger;
import java.security.PrivateKey;

/**
 * This model class holds the value of an encryption private key
 */
public class EncryptionPrivateKey implements PrivateKey {
    private final BigInteger privateKey;
    private final EncryptionGroup encryptionGroup;
    private transient final Conversion conversion = new Conversion();

    public EncryptionPrivateKey(BigInteger privateKey, EncryptionGroup encryptionGroup) {
        this.privateKey = privateKey;
        this.encryptionGroup = encryptionGroup;
    }

    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return conversion.toByteArray(privateKey);
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public EncryptionGroup getEncryptionGroup() {
        return encryptionGroup;
    }
}
