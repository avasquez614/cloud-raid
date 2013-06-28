package org.cloudraid.ida.persistence.crypto.jce;

import org.cloudraid.ida.persistence.crypto.Decryptor;
import org.cloudraid.ida.persistence.exception.CryptoException;

import javax.crypto.Cipher;

/**
 * {@link Decryptor} implementation that uses the Java Cryptography Extension (JCE).
 *
 * @author avasquez
 */
public class JceDecryptor implements Decryptor {

    private JceEncryptionParams params;

    public JceDecryptor(JceEncryptionParams params) {
        this.params = params;
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) throws CryptoException {
        Cipher cipher = params.getCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, params.getSecretKey(), params.getIvParameterSpec());
        } catch (Exception e) {
            throw new CryptoException("Unable to initialize cipher", e);
        }

        try {
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

}
