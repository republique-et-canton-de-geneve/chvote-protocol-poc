package ch.ge.ve.protopoc.service.model;

import ch.ge.ve.protopoc.service.support.Conversion;

import java.math.BigInteger;
import java.security.PrivateKey;

/**
 * Missing javadoc!
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
