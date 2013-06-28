package org.cloudraid.ida.persistence.crypto;

import org.cloudraid.ida.persistence.exception.CryptoException;

/**
 * Encrypts using an internal, implementation-specific encryption algorithm.
 *
 * @author avasquez
 */
public interface Encryptor {

    /**
     * Encrypts the specified data.
     *
     * @param data
     *          the data to encrypt
     * @return the encrypted data
     * @throws CryptoException
     */
    byte[] encrypt(byte[] data) throws CryptoException;

}
