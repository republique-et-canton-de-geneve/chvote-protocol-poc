package ch.ge.ve.protopoc.service.algorithm;

import ch.ge.ve.protopoc.service.model.EncryptionGroup;
import ch.ge.ve.protopoc.service.model.EncryptionPrivateKey;
import ch.ge.ve.protopoc.service.model.EncryptionPublicKey;
import ch.ge.ve.protopoc.service.support.Conversion;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

/**
 * Missing javadoc!
 */
public class KeyEstablishment {
    private final SecureRandom secureRandom;
    private transient final Conversion conversion = new Conversion();

    public KeyEstablishment(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Algorithm 5.7: genKeyPair
     *
     * @param eg the encryption group for which we need a {@link KeyPair}
     * @return a newly, randomly generated KeyPair
     */
    public KeyPair generateKeyPair(EncryptionGroup eg) {
        int byteLength = (int) Math.ceil(eg.q.bitLength() / 8.0);
        byte[] bytes = new byte[byteLength * 2]; // Can't achieve perfectly uniform distribution --> how to limit bias?
        secureRandom.nextBytes(bytes);
        BigInteger sk = conversion.toInteger(bytes).mod(eg.q);
        BigInteger pk = eg.g.modPow(sk, eg.p);

        return new KeyPair(new EncryptionPublicKey(pk, eg), new EncryptionPrivateKey(sk, eg));
    }
}
