package org.cloudraid.ida.persistence.crypto;

import org.cloudraid.ida.persistence.exception.CryptoException;

/**
 * Dencrypts using an internal, implementation-specific encryption algorithm.
 *
 * @author avasquez
 */
public interface Decryptor {
    /**
     * Decrypts the specified encrypted data to obtain the original data.
     *
     * @param encryptedData
     *          the encrypted data to decrypt
     * @return the decrypted, original data
     * @throws CryptoException
     */
    byte[] decrypt(byte[] encryptedData) throws CryptoException;

}
