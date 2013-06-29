package org.cloudraid.ida.persistence.crypto.jce;

import org.cloudraid.ida.persistence.crypto.EncryptionParams;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * {@link org.cloudraid.ida.persistence.crypto.EncryptionParams} implementation for JCE related classes.
 */
public class JceEncryptionParams implements EncryptionParams {

    private Cipher cipher;
    private SecretKey secretKey;
    private IvParameterSpec ivParameterSpec;

    public JceEncryptionParams(Cipher cipher, SecretKey secretKey, IvParameterSpec ivParameterSpec) {
        this.cipher = cipher;
        this.secretKey = secretKey;
        this.ivParameterSpec = ivParameterSpec;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public IvParameterSpec getIvParameterSpec() {
        return ivParameterSpec;
    }

    @Override
    public byte[] getKey() {
        return secretKey.getEncoded();
    }

    @Override
    public byte[] getIv() {
        return ivParameterSpec.getIV();
    }

}
