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

package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.model.EncryptionPrivateKey;
import ch.ge.ve.protopoc.service.model.EncryptionPublicKey;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.List;

import static ch.ge.ve.protopoc.arithmetic.BigIntegerArithmetic.modExp;

/**
 * Algorithms used during the key establishment part of the election preparation phase
 */
public class KeyEstablishmentAlgorithms {
    private final RandomGenerator randomGenerator;

    public KeyEstablishmentAlgorithms(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    /**
     * Algorithm 7.15: genKeyPair
     *
     * @param eg the encryption group for which we need a {@link KeyPair}
     * @return a newly, randomly generated KeyPair
     */
    public KeyPair generateKeyPair(EncryptionGroup eg) {
        BigInteger sk = randomGenerator.randomInZq(eg.getQ());
        BigInteger pk = modExp(eg.getG(), sk, eg.getP());

        return new KeyPair(new EncryptionPublicKey(pk, eg), new EncryptionPrivateKey(sk, eg));
    }

    /**
     * Algorithm 7.16: GetPublicKey
     *
     * @param publicKeys the set of public key shares that should be combined
     * @return the combined public key
     */
    public EncryptionPublicKey getPublicKey(List<EncryptionPublicKey> publicKeys) {
        BigInteger publicKey = BigInteger.ONE;
        EncryptionGroup eg = publicKeys.get(0).getEncryptionGroup();
        Preconditions.checkArgument(publicKeys.stream().allMatch(pk -> pk.getEncryptionGroup().equals(eg)),
                "All of the public keys should be defined within the same encryption group");

        for (EncryptionPublicKey key : publicKeys) {
            publicKey = publicKey.multiply(key.getPublicKey()).mod(eg.getP());
        }
        return new EncryptionPublicKey(publicKey, eg);
    }
}
