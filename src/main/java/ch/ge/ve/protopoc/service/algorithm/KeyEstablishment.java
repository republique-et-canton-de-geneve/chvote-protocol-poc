package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.model.EncryptionPrivateKey;
import ch.ge.ve.protopoc.service.model.EncryptionPublicKey;
import ch.ge.ve.protopoc.service.support.Conversion;
import ch.ge.ve.protopoc.service.support.RandomGenerator;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

/**
 * Missing javadoc!
 */
public class KeyEstablishment {
    private final SecureRandom secureRandom;
    private final RandomGenerator randomGenerator;
    private transient final Conversion conversion = new Conversion();

    public KeyEstablishment(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        randomGenerator = new RandomGenerator(secureRandom);
    }

    /**
     * Algorithm 5.7: genKeyPair
     *
     * @param eg the encryption group for which we need a {@link KeyPair}
     * @return a newly, randomly generated KeyPair
     */
    public KeyPair generateKeyPair(EncryptionGroup eg) {
        BigInteger sk = randomGenerator.randomBigInteger(eg.q);
        BigInteger pk = eg.g.modPow(sk, eg.p);

        return new KeyPair(new EncryptionPublicKey(pk, eg), new EncryptionPrivateKey(sk, eg));
    }

    /**
     * Algorithm 5.8: GetPublicKey
     * @param publicKeys the set of public key shares that should be combined
     * @return the combined public key
     */
    public EncryptionPublicKey getPublicKey(EncryptionPublicKey... publicKeys) {
        // check all encryption groups are equal
        BigInteger publicKey = BigInteger.ONE;
        EncryptionGroup eg = publicKeys[0].getEncryptionGroup();
        for (EncryptionPublicKey key : publicKeys) {
            publicKey = publicKey.multiply(key.getPublicKey()).mod(eg.p);
        }
        return new EncryptionPublicKey(publicKey, eg);
    }
}
