package org.cloudraid.ida.persistence.crypto.jce;

import org.cloudraid.ida.persistence.crypto.Encryptor;
import org.cloudraid.ida.persistence.exception.CryptoException;

import javax.crypto.Cipher;

/**
 * {@link Encryptor} implementation that uses the Java Cryptography Extension (JCE).
 *
 * @author avasquez
 */
public class JceEncryptor implements Encryptor {

    private JceEncryptionParams params;

    public JceEncryptor(JceEncryptionParams params) {
        this.params = params;
    }

    @Override
    public byte[] encrypt(byte[] data) throws CryptoException {
        Cipher cipher = params.getCipher();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, params.getSecretKey(), params.getIvParameterSpec());
        } catch (Exception e) {
            throw new CryptoException("Unable to initialize cipher", e);
        }

        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

}
