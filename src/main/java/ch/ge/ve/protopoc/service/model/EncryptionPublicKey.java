package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.Conversion;

import java.math.BigInteger;
import java.security.PublicKey;

/**
 * Missing javadoc!
 */
public class EncryptionPublicKey implements PublicKey {
    private final BigInteger publicKey;
    private final EncryptionGroup encryptionGroup;
    private transient final Conversion conversion = new Conversion();

    public EncryptionPublicKey(BigInteger publicKey, EncryptionGroup encryptionGroup) {
        this.publicKey = publicKey;
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
        return conversion.toByteArray(publicKey);
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public EncryptionGroup getEncryptionGroup() {
        return encryptionGroup;
    }
}
